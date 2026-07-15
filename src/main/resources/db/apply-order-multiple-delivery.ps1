$ErrorActionPreference = 'Stop'

$secretPath = Join-Path $env:USERPROFILE '.kmarket\application-secrets.properties'
$applicationPath = Join-Path $PSScriptRoot '..\application.properties'
$migrationPath = Join-Path $PSScriptRoot 'order-multiple-delivery.sql'

function Read-Properties([string]$path) {
    $values = @{}
    foreach ($line in Get-Content -LiteralPath $path -Encoding UTF8) {
        if ($line -match '^\s*([^#][^=]*)=(.*)$') {
            $values[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
    return $values
}

if (-not (Test-Path -LiteralPath $secretPath)) {
    throw 'DB 비밀 설정 파일을 찾을 수 없습니다.'
}

$app = Read-Properties $applicationPath
$secret = Read-Properties $secretPath
$url = if ($secret.DB_URL) { $secret.DB_URL } elseif ($secret['spring.datasource.url']) { $secret['spring.datasource.url'] } else { $app['spring.datasource.url'] }
$url = $url -replace '^\$\{DB_URL:', '' -replace '\}$', ''
$username = if ($secret.DB_USERNAME) { $secret.DB_USERNAME } elseif ($secret['spring.datasource.username']) { $secret['spring.datasource.username'] } else { 'project' }
$password = if ($secret.DB_PASSWORD) { $secret.DB_PASSWORD } else { $secret['spring.datasource.password'] }

if (-not $password) { throw 'DB_PASSWORD 설정이 없습니다.' }
if ($url -notmatch '^jdbc:mysql://([^:/]+)(?::(\d+))?/([^?]+)') {
    throw 'MySQL 접속 URL 형식을 해석할 수 없습니다.'
}

$hostName = $matches[1]
$port = if ($matches[2]) { $matches[2] } else { '3306' }
$database = $matches[3]
$previousPassword = $env:MYSQL_PWD

try {
    $env:MYSQL_PWD = $password
    Get-Content -LiteralPath $migrationPath -Raw -Encoding UTF8 |
        & mysql --default-character-set=utf8mb4 --host=$hostName --port=$port --user=$username --database=$database
    if ($LASTEXITCODE -ne 0) { throw 'DB 스키마 적용에 실패했습니다.' }

    $verification = & mysql --batch --skip-column-names --host=$hostName --port=$port --user=$username --database=$database `
        --execute="SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order' AND COLUMN_NAME IN ('ordererName','ordererPhone','ordererZipCode','ordererBaseAddress','ordererDetailAddress'); SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='order_delivery';"
    if ($LASTEXITCODE -ne 0) { throw 'DB 스키마 확인에 실패했습니다.' }
    $counts = @($verification | ForEach-Object { [int]$_ })
    if ($counts.Count -lt 2 -or $counts[0] -ne 5 -or $counts[1] -ne 1) {
        throw '필요한 컬럼 또는 테이블이 모두 생성되지 않았습니다.'
    }
    Write-Output 'ORDER_SCHEMA_READY'
}
finally {
    $env:MYSQL_PWD = $previousPassword
}
