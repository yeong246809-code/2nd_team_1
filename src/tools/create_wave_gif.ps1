param(
    [Parameter(Mandatory = $true)][string]$InputPath,
    [Parameter(Mandatory = $true)][string]$OutputPath,
    [int]$Width = 1280,
    [int]$FrameCount = 24
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName PresentationCore
Add-Type -AssemblyName WindowsBase

function New-BubbleBrush([int]$alpha, [int]$red, [int]$green, [int]$blue) {
    return [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb($alpha, $red, $green, $blue))
}

function Convert-ToGifFrame([System.Drawing.Bitmap]$bitmap, [bool]$firstFrame) {
    $stream = [System.IO.MemoryStream]::new()
    $bitmap.Save($stream, [System.Drawing.Imaging.ImageFormat]::Png)
    $stream.Position = 0
    $decoder = [System.Windows.Media.Imaging.PngBitmapDecoder]::new(
        $stream,
        [System.Windows.Media.Imaging.BitmapCreateOptions]::PreservePixelFormat,
        [System.Windows.Media.Imaging.BitmapCacheOption]::OnLoad)
    $source = $decoder.Frames[0]
    $metadata = [System.Windows.Media.Imaging.BitmapMetadata]::new('gif')
    $metadata.SetQuery('/grctlext/Delay', [UInt16]8)
    $metadata.SetQuery('/grctlext/Disposal', [byte]2)
    if ($firstFrame) {
        $metadata.SetQuery('/appext/Application', [byte[]](0x4E,0x45,0x54,0x53,0x43,0x41,0x50,0x45,0x32,0x2E,0x30))
        $metadata.SetQuery('/appext/Data', [byte[]](0x03,0x01,0x00,0x00,0x00))
    }
    $frame = [System.Windows.Media.Imaging.BitmapFrame]::Create($source, $null, $metadata, $null)
    $stream.Dispose()
    return $frame
}

function Add-GifAnimationMetadata([string]$path, [UInt16]$delayCentiseconds) {
    $bytes = [System.IO.File]::ReadAllBytes($path)
    if ($bytes.Length -lt 14 -or [System.Text.Encoding]::ASCII.GetString($bytes, 0, 6) -notlike 'GIF*') {
        throw '생성된 파일이 올바른 GIF가 아닙니다.'
    }

    $position = 13
    $packed = $bytes[10]
    if (($packed -band 0x80) -ne 0) {
        $globalColorTableSize = 3 * [Math]::Pow(2, (($packed -band 0x07) + 1))
        $position += [int]$globalColorTableSize
    }

    $output = [System.IO.MemoryStream]::new()
    try {
        $output.Write($bytes, 0, $position)

        # NETSCAPE2.0 확장: GIF를 무한 반복한다.
        $loopExtension = [byte[]](0x21, 0xFF, 0x0B, 0x4E, 0x45, 0x54, 0x53, 0x43, 0x41, 0x50, 0x45, 0x32, 0x2E, 0x30, 0x03, 0x01, 0x00, 0x00, 0x00)
        $output.Write($loopExtension, 0, $loopExtension.Length)

        while ($position -lt $bytes.Length) {
            $marker = $bytes[$position]

            if ($marker -eq 0x3B) {
                $output.WriteByte($marker)
                break
            }

            if ($marker -eq 0x21) {
                # 기존 확장 블록을 크기 단위로 안전하게 복사한다.
                $output.Write($bytes, $position, 2)
                $position += 2
                while ($position -lt $bytes.Length) {
                    $blockSize = [int]$bytes[$position]
                    $output.Write($bytes, $position, $blockSize + 1)
                    $position += $blockSize + 1
                    if ($blockSize -eq 0) { break }
                }
                continue
            }

            if ($marker -eq 0x2C) {
                # 각 이미지 프레임 앞에 8/100초 지연과 배경 복원 방식을 지정한다.
                $gce = [byte[]](0x21, 0xF9, 0x04, 0x08, ($delayCentiseconds -band 0xFF), (($delayCentiseconds -shr 8) -band 0xFF), 0x00, 0x00)
                $output.Write($gce, 0, $gce.Length)

                $descriptorStart = $position
                $imagePacked = $bytes[$descriptorStart + 9]
                $descriptorLength = 10
                $output.Write($bytes, $position, $descriptorLength)
                $position += $descriptorLength

                if (($imagePacked -band 0x80) -ne 0) {
                    $localColorTableSize = 3 * [Math]::Pow(2, (($imagePacked -band 0x07) + 1))
                    $output.Write($bytes, $position, [int]$localColorTableSize)
                    $position += [int]$localColorTableSize
                }

                # LZW 최소 코드 크기와 이어지는 압축 데이터 서브블록을 복사한다.
                $output.WriteByte($bytes[$position])
                $position++
                while ($position -lt $bytes.Length) {
                    $blockSize = [int]$bytes[$position]
                    $output.Write($bytes, $position, $blockSize + 1)
                    $position += $blockSize + 1
                    if ($blockSize -eq 0) { break }
                }
                continue
            }

            throw ("알 수 없는 GIF 블록 표식: 0x{0:X2}" -f $marker)
        }

        [System.IO.File]::WriteAllBytes($path, $output.ToArray())
    }
    finally { $output.Dispose() }
}

$source = [System.Drawing.Bitmap]::FromFile($InputPath)
try {
    $height = [int][Math]::Round($Width * $source.Height / $source.Width)
    $base = [System.Drawing.Bitmap]::new($Width, $height, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
    $baseGraphics = [System.Drawing.Graphics]::FromImage($base)
    try {
        $baseGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $baseGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $baseGraphics.DrawImage($source, 0, 0, $Width, $height)
    }
    finally { $baseGraphics.Dispose() }

    $encoder = [System.Windows.Media.Imaging.GifBitmapEncoder]::new()
    $twoPi = [Math]::PI * 2
    $bubbleSeeds = @(
        @(0.54, 0.56, 10, 0.02), @(0.58, 0.38, 15, 0.18), @(0.61, 0.22, 9, 0.39),
        @(0.64, 0.48, 20, 0.62), @(0.67, 0.30, 12, 0.81), @(0.70, 0.14, 18, 0.08),
        @(0.73, 0.43, 9, 0.27), @(0.76, 0.23, 14, 0.50), @(0.79, 0.52, 17, 0.73),
        @(0.82, 0.34, 11, 0.94), @(0.85, 0.17, 21, 0.35), @(0.88, 0.46, 13, 0.57),
        @(0.91, 0.27, 18, 0.78), @(0.94, 0.53, 10, 0.12), @(0.97, 0.36, 15, 0.44),
        @(0.72, 0.61, 8, 0.88), @(0.84, 0.59, 9, 0.67), @(0.96, 0.18, 12, 0.23)
    )

    for ($frameIndex = 0; $frameIndex -lt $FrameCount; $frameIndex++) {
        $phase = $twoPi * $frameIndex / $FrameCount
        $frame = [System.Drawing.Bitmap]::new($Width, $height, [System.Drawing.Imaging.PixelFormat]::Format24bppRgb)
        $graphics = [System.Drawing.Graphics]::FromImage($frame)
        try {
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $graphics.DrawImageUnscaled($base, 0, 0)

            # 크고 선명한 거품이 각기 다른 속도로 떠오르며 자연스러운 루프를 만든다.
            foreach ($seed in $bubbleSeeds) {
                $progress = ($frameIndex / [double]$FrameCount + $seed[3]) % 1.0
                $x = [float]($Width * $seed[0] + [Math]::Sin($phase + $seed[3] * $twoPi) * 11)
                $y = [float]($height * ($seed[1] + 0.19 - $progress * 0.19))
                $radius = [float]($seed[2] * ($Width / 1280.0))
                $alpha = [int](52 + 74 * [Math]::Sin([Math]::PI * $progress))
                $fill = New-BubbleBrush $alpha 210 247 255
                $outline = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb([Math]::Min($alpha + 85, 220), 255, 255, 255), 2.0)
                try {
                    $graphics.FillEllipse($fill, $x - $radius, $y - $radius, $radius * 2, $radius * 2)
                    $graphics.DrawEllipse($outline, $x - $radius, $y - $radius, $radius * 2, $radius * 2)
                    $innerOutline = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb([Math]::Min($alpha + 20, 160), 110, 225, 245), 1.0)
                    $highlight = New-BubbleBrush ([Math]::Min($alpha + 100, 235)) 255 255 255
                    try {
                        $graphics.DrawEllipse($innerOutline, $x - $radius * 0.80, $y - $radius * 0.80, $radius * 1.60, $radius * 1.60)
                        $graphics.FillEllipse($highlight, $x - $radius * 0.48, $y - $radius * 0.53, $radius * 0.42, $radius * 0.42)
                    }
                    finally { $highlight.Dispose() }
                }
                finally {
                    $fill.Dispose()
                    $outline.Dispose()
                    if ($innerOutline) { $innerOutline.Dispose() }
                }
            }
        }
        finally { $graphics.Dispose() }

        try { $encoder.Frames.Add((Convert-ToGifFrame $frame ($frameIndex -eq 0))) }
        finally { $frame.Dispose() }
    }

    $outputDirectory = Split-Path -Parent $OutputPath
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
    $file = [System.IO.File]::Open($OutputPath, [System.IO.FileMode]::Create)
    try { $encoder.Save($file) } finally { $file.Dispose() }
    Add-GifAnimationMetadata $OutputPath 8
}
finally {
    if ($base) { $base.Dispose() }
    $source.Dispose()
}

$result = Get-Item -LiteralPath $OutputPath
Write-Output ("GIF={0}" -f $result.FullName)
Write-Output ("BYTES={0}" -f $result.Length)
