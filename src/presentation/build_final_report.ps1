$ErrorActionPreference = 'Stop'

function RGB([int]$r, [int]$g, [int]$b) { return $r + 256 * $g + 65536 * $b }

$C = @{
    Navy = RGB 18 44 69
    Blue = RGB 40 102 156
    Cyan = RGB 42 171 190
    Sky = RGB 225 243 247
    Orange = RGB 244 145 74
    Green = RGB 65 157 120
    Red = RGB 211 85 85
    Ink = RGB 29 42 55
    Muted = RGB 92 108 122
    Line = RGB 214 224 231
    Pale = RGB 244 248 251
    White = RGB 255 255 255
}

$outDir = Join-Path $PSScriptRoot 'output'
$previewDir = Join-Path $outDir 'preview'
New-Item -ItemType Directory -Path $outDir -Force | Out-Null
New-Item -ItemType Directory -Path $previewDir -Force | Out-Null

$ppt = New-Object -ComObject PowerPoint.Application
$ppt.Visible = -1
$pres = $ppt.Presentations.Add()
$pres.PageSetup.SlideWidth = 960
$pres.PageSetup.SlideHeight = 540

function Add-Text($slide, [string]$text, [float]$x, [float]$y, [float]$w, [float]$h,
    [float]$size = 18, [int]$color = $C.Ink, [bool]$bold = $false,
    [int]$align = 1, [string]$font = '맑은 고딕') {
    $shape = $slide.Shapes.AddTextbox(1, $x, $y, $w, $h)
    $shape.TextFrame.MarginLeft = 0
    $shape.TextFrame.MarginRight = 0
    $shape.TextFrame.MarginTop = 0
    $shape.TextFrame.MarginBottom = 0
    $shape.TextFrame.WordWrap = -1
    $shape.TextFrame.TextRange.Text = $text
    $shape.TextFrame.TextRange.Font.Name = $font
    $shape.TextFrame.TextRange.Font.NameFarEast = $font
    $shape.TextFrame.TextRange.Font.Size = $size
    $shape.TextFrame.TextRange.Font.Color.RGB = $color
    $shape.TextFrame.TextRange.Font.Bold = [int]$bold * -1
    $shape.TextFrame.TextRange.ParagraphFormat.Alignment = $align
    return $shape
}

function Add-Rect($slide, [float]$x, [float]$y, [float]$w, [float]$h,
    [int]$fill = $C.White, [int]$line = $C.Line, [float]$radius = 0) {
    $type = if ($radius -gt 0) { 5 } else { 1 }
    $shape = $slide.Shapes.AddShape($type, $x, $y, $w, $h)
    $shape.Fill.ForeColor.RGB = $fill
    $shape.Line.ForeColor.RGB = $line
    $shape.Line.Weight = 1
    return $shape
}

function Add-Line($slide, [float]$x1, [float]$y1, [float]$x2, [float]$y2,
    [int]$color = $C.Line, [float]$weight = 1.5, [bool]$arrow = $false) {
    $line = $slide.Shapes.AddLine($x1, $y1, $x2, $y2)
    $line.Line.ForeColor.RGB = $color
    $line.Line.Weight = $weight
    if ($arrow) { $line.Line.EndArrowheadStyle = 3 }
    return $line
}

function Add-Circle($slide, [string]$text, [float]$x, [float]$y, [float]$d,
    [int]$fill, [int]$textColor = $C.White, [float]$size = 18) {
    $s = $slide.Shapes.AddShape(9, $x, $y, $d, $d)
    $s.Fill.ForeColor.RGB = $fill
    $s.Line.Visible = 0
    $s.TextFrame.VerticalAnchor = 3
    $s.TextFrame.TextRange.Text = $text
    $s.TextFrame.TextRange.Font.Name = '맑은 고딕'
    $s.TextFrame.TextRange.Font.NameFarEast = '맑은 고딕'
    $s.TextFrame.TextRange.Font.Size = $size
    $s.TextFrame.TextRange.Font.Bold = -1
    $s.TextFrame.TextRange.Font.Color.RGB = $textColor
    $s.TextFrame.TextRange.ParagraphFormat.Alignment = 2
    return $s
}

function New-Slide([string]$title, [string]$section = '') {
    $slide = $pres.Slides.Add($pres.Slides.Count + 1, 12)
    $bg = $slide.Background.Fill
    $bg.ForeColor.RGB = $C.White
    $bg.Solid()
    $bgShape = Add-Rect $slide 0 0 960 540 $C.White $C.White
    $bgShape.Line.Visible = 0
    Add-Rect $slide 0 0 12 540 $C.Cyan $C.Cyan | Out-Null
    if ($section) {
        $tag = Add-Rect $slide 54 31 110 25 $C.Sky $C.Sky 6
        Add-Text $slide $section 54 34 110 18 10 $C.Blue $true 2 | Out-Null
    }
    Add-Text $slide $title 54 68 850 52 27 $C.Navy $true | Out-Null
    Add-Line $slide 54 121 906 121 $C.Line 1 | Out-Null
    Add-Text $slide ("K-Market  |  " + $pres.Slides.Count.ToString('00')) 760 512 146 14 9 $C.Muted $false 3 | Out-Null
    return $slide
}

function Add-Card($slide, [string]$title, [string]$body, [float]$x, [float]$y, [float]$w, [float]$h,
    [int]$accent = $C.Blue) {
    Add-Rect $slide $x $y $w $h $C.White $C.Line 8 | Out-Null
    Add-Rect $slide $x $y 7 $h $accent $accent 4 | Out-Null
    Add-Text $slide $title ($x + 22) ($y + 18) ($w - 38) 27 17 $C.Navy $true | Out-Null
    Add-Text $slide $body ($x + 22) ($y + 54) ($w - 38) ($h - 68) 12.5 $C.Muted $false | Out-Null
}

function Add-Pill($slide, [string]$text, [float]$x, [float]$y, [float]$w, [int]$fill = $C.Sky, [int]$color = $C.Blue) {
    Add-Rect $slide $x $y $w 28 $fill $fill 10 | Out-Null
    Add-Text $slide $text $x ($y + 5) $w 17 11 $color $true 2 | Out-Null
}

# 1. Cover
$s = $pres.Slides.Add(1, 12)
$s.Background.Fill.ForeColor.RGB = $C.Navy
$s.Background.Fill.Solid()
$coverBg = Add-Rect $s 0 0 960 540 $C.Navy $C.Navy
$coverBg.Line.Visible = 0
Add-Rect $s 0 0 18 540 $C.Cyan $C.Cyan | Out-Null
Add-Text $s 'SPRING BOOT TEAM PROJECT' 70 72 420 24 12 $C.Cyan $true | Out-Null
Add-Text $s 'K-Market' 68 126 600 78 48 $C.White $true | Out-Null
Add-Text $s '쇼핑몰 웹 서비스 최종 발표 보고서' 70 210 610 44 24 (RGB 222 235 244) $false | Out-Null
Add-Line $s 70 290 890 290 (RGB 75 107 132) 1 | Out-Null
Add-Text $s 'Spring MVC · Security · JPA · QueryDSL · MyBatis · Thymeleaf' 70 316 800 28 15 (RGB 171 200 218) $false | Out-Null
Add-Text $s '팀원  강현주 · 이찬영 · 남수아 · 강재은 · 한성주' 70 430 680 24 14 $C.White $true | Out-Null
Add-Text $s '최종 발표' 70 466 200 20 11 $C.Cyan $true | Out-Null
Add-Circle $s 'K' 790 86 104 $C.Cyan $C.Navy 42 | Out-Null

# 2. Agenda
$s = New-Slide '발표보고서 목차' 'CONTENTS'
$agenda = @(
    @('01','프로젝트 개요'), @('02','팀원 소개'), @('03','프로젝트 개발 일정'), @('04','프로젝트 아키텍처'),
    @('05','프로젝트 구조'), @('06','프로젝트 ERD'), @('07','프로젝트 시연'), @('08','질문 및 답변')
)
for ($i=0; $i -lt $agenda.Count; $i++) {
    $col = $i % 2; $row = [math]::Floor($i / 2)
    $x = 72 + $col * 425; $y = 150 + $row * 76
    Add-Circle $s $agenda[$i][0] $x $y 42 $(if($i -eq 6){$C.Orange}else{$C.Blue}) $C.White 11 | Out-Null
    Add-Text $s $agenda[$i][1] ($x+58) ($y+7) 310 27 17 $C.Ink $true | Out-Null
    Add-Line $s ($x+58) ($y+40) ($x+360) ($y+40) $C.Line 1 | Out-Null
}

# 3. Overview
$s = New-Slide '프로젝트 개요' '01  PROJECT OVERVIEW'
Add-Text $s '사용자·판매자·관리자를 하나의 서비스로 연결한 종합 쇼핑몰' 54 142 850 34 21 $C.Blue $true | Out-Null
Add-Card $s '사용자' '상품 탐색과 검색
장바구니·주문·결제
마이페이지·리뷰·문의' 54 202 250 190 $C.Cyan
Add-Card $s '판매자' '상품·옵션·재고 관리
주문과 배송 처리
매출 현황 확인' 355 202 250 190 $C.Orange
Add-Card $s '관리자' '회원·상점·쿠폰 관리
배너·카테고리·정책 설정
고객센터와 통계 관리' 656 202 250 190 $C.Green
Add-Pill $s '총 73개 기능/페이지' 54 425 170 $C.Navy $C.White
Add-Pill $s '30 Controllers' 240 425 145
Add-Pill $s '43 Services' 400 425 125
Add-Pill $s '31 Entities' 540 425 125
Add-Pill $s '70 Templates' 680 425 145

# 4. Goal
$s = New-Slide '프로젝트 목표와 핵심 가치' '01  PROJECT OVERVIEW'
Add-Card $s '학습 목표' 'Spring MVC 계층 구조를 실제 도메인에 적용하고, 인증·인가와 데이터 접근 기술을 통합한다.' 54 154 400 125 $C.Blue
Add-Card $s '서비스 목표' '쇼핑의 핵심 흐름부터 판매자 운영, 관리자 통제까지 하나의 웹 서비스로 완성한다.' 506 154 400 125 $C.Cyan
Add-Card $s '팀 개발 목표' '기능 단위로 역할을 분담하고 Git 기반 병합과 통합 테스트를 경험한다.' 54 312 400 125 $C.Orange
Add-Card $s '품질 목표' '트랜잭션, 권한, 재고, 이미지 저장 등 실제 서비스에서 발생하는 문제를 해결한다.' 506 312 400 125 $C.Green

# 5. Team
$s = New-Slide '팀원 소개 및 업무 배정 현황' '02  TEAM'
$headers = @('담당자','배정 수','주요 담당 영역','진행 상태')
$widths = @(110,95,500,115); $x0=70; $y0=150; $rowH=52
$x=$x0
for($i=0;$i -lt 4;$i++){
    Add-Rect $s $x $y0 $widths[$i] 38 $C.Navy $C.White | Out-Null
    Add-Text $s $headers[$i] $x ($y0+9) $widths[$i] 18 12 $C.White $true 2 | Out-Null
    $x += $widths[$i]
}
$members = @(
    @('강현주','11개','메인, 상품(목록/상세/주문), 회사소개'),
    @('이찬영','16개','회원가입/로그인, 마이페이지 전체'),
    @('남수아','25개','고객센터(사용자/관리자), 사이트 정책·약관'),
    @('강재은','11개','관리자 메인, 환경설정, 상점·회원관리'),
    @('한성주','10개','관리자 매출, 상품/주문/배송/쿠폰관리')
)
for($r=0;$r -lt $members.Count;$r++){
    $y=$y0+38+$r*$rowH; $fill=if($r%2 -eq 0){$C.Pale}else{$C.White}; $x=$x0
    for($i=0;$i -lt 4;$i++){
        Add-Rect $s $x $y $widths[$i] $rowH $fill $C.Line | Out-Null
        $value = if($i -eq 3){'배정완료'}else{$members[$r][$i]}
        $align = if($i -eq 2){1}else{2}
        $color = if($i -eq 3){$C.Green}else{$C.Ink}
        Add-Text $s $value ($x+$(if($i -eq 2){12}else{0})) ($y+16) ($widths[$i]-$(if($i -eq 2){18}else{0})) 20 11.5 $color ($i -eq 0 -or $i -eq 3) $align | Out-Null
        $x += $widths[$i]
    }
}
Add-Text $s '합계  73개 기능/페이지' 680 462 210 24 13 $C.Blue $true 3 | Out-Null

# 6. Schedule
$s = New-Slide '프로젝트 개발 일정' '03  SCHEDULE'
$phases = @(
    @('01','요구사항 분석','기능 정의 · 역할 배정'),
    @('02','설계','화면 · DB · URL 구조'),
    @('03','공통 기반','회원 · 보안 · 레이아웃'),
    @('04','기능 개발','사용자 · 판매자 · 관리자'),
    @('05','통합','병합 · 오류 수정 · 연동'),
    @('06','마무리','테스트 · 배포 · 발표 준비')
)
for($i=0;$i -lt $phases.Count;$i++){
    $x=62+$i*146
    if($i -lt 5){ Add-Line $s ($x+56) 232 ($x+146) 232 $C.Cyan 3 $true | Out-Null }
    Add-Circle $s $phases[$i][0] $x 204 56 $(if($i -eq 5){$C.Orange}else{$C.Blue}) $C.White 12 | Out-Null
    Add-Text $s $phases[$i][1] ($x-25) 284 106 24 14 $C.Navy $true 2 | Out-Null
    Add-Text $s $phases[$i][2] ($x-36) 318 128 54 10.5 $C.Muted $false 2 | Out-Null
}
Add-Rect $s 70 406 820 55 $C.Pale $C.Line 8 | Out-Null
Add-Text $s '※ 실제 수행 날짜를 발표 전 팀 일정에 맞게 입력' 92 424 780 20 12 $C.Muted $false 2 | Out-Null

# 7. Architecture
$s = New-Slide '프로젝트 아키텍처' '04  ARCHITECTURE'
$layers = @(
    @('CLIENT','Browser','Thymeleaf · HTML/CSS/JS',$C.Cyan),
    @('WEB','Controller','요청 매핑 · 검증 · View Model',$C.Blue),
    @('DOMAIN','Service','비즈니스 로직 · @Transactional',$C.Orange),
    @('DATA','Repository / DAO','JPA · QueryDSL · MyBatis',$C.Green)
)
for($i=0;$i -lt $layers.Count;$i++){
    $y=145+$i*78
    Add-Rect $s 104 $y 530 58 $C.White $C.Line 7 | Out-Null
    Add-Rect $s 104 $y 96 58 $layers[$i][3] $layers[$i][3] 7 | Out-Null
    Add-Text $s $layers[$i][0] 104 ($y+19) 96 20 11 $C.White $true 2 | Out-Null
    Add-Text $s $layers[$i][1] 222 ($y+10) 175 22 16 $C.Navy $true | Out-Null
    Add-Text $s $layers[$i][2] 222 ($y+34) 385 17 10.5 $C.Muted | Out-Null
    if($i -lt 3){Add-Line $s 369 ($y+58) 369 ($y+77) $C.Cyan 2 $true | Out-Null}
}
Add-Card $s '외부 연동' 'MySQL
AWS S3
SMTP Mail
Kakao · Naver · Google OAuth2' 682 145 224 215 $C.Cyan
Add-Pill $s 'Spring Security' 700 390 170 $C.Navy $C.White
Add-Text $s '역할 기반 접근 제어
USER · SELLER · ADMIN' 700 430 170 42 11 $C.Muted $false 2 | Out-Null

# 8. Tech Stack
$s = New-Slide '적용 기술 스택' '04  ARCHITECTURE'
$tech = @(
    @('Backend','Java 17 · Spring Boot 3.5 · Spring MVC'),
    @('Security','Spring Security · OAuth2 Client'),
    @('Data','JPA · QueryDSL · MyBatis · MySQL'),
    @('View','Thymeleaf · HTML · CSS · JavaScript'),
    @('Infra','AWS S3 · SMTP · EC2 환경 설정'),
    @('Collaboration','Git · GitHub · Gradle')
)
for($i=0;$i -lt $tech.Count;$i++){
    $col=$i%2; $row=[math]::Floor($i/2); $x=64+$col*430; $y=150+$row*105
    $accent=@($C.Blue,$C.Cyan,$C.Green,$C.Orange,$C.Red,$C.Navy)[$i]
    Add-Card $s $tech[$i][0] $tech[$i][1] $x $y 400 82 $accent
}

# 9. Project structure
$s = New-Slide '프로젝트 정보구조' '05  STRUCTURE'
Add-Text $s '역할별 화면과 기능을 분리하고 공통 계층을 공유' 54 142 700 28 18 $C.Blue $true | Out-Null
$groups = @(
    @('사용자 영역','/member  /product  /my','회원 · 상품 · 장바구니 · 주문 · 마이페이지',$C.Cyan),
    @('운영 영역','/admin','상품 · 주문 · 배송 · 쿠폰 · 회원 · 상점 · 환경설정',$C.Orange),
    @('고객지원','/cs  /policy  /company','공지 · FAQ · Q&A · 약관 · 회사소개',$C.Green)
)
for($i=0;$i -lt 3;$i++){
    $y=196+$i*92
    Add-Circle $s ($i+1).ToString('00') 64 ($y+4) 48 $groups[$i][3] $C.White 11 | Out-Null
    Add-Text $s $groups[$i][0] 132 $y 150 24 16 $C.Navy $true | Out-Null
    Add-Pill $s $groups[$i][1] 285 ($y-3) 190 $C.Pale $C.Blue
    Add-Text $s $groups[$i][2] 500 $y 390 44 11.5 $C.Muted | Out-Null
}
Add-Line $s 88 248 88 292 $C.Line 2 | Out-Null
Add-Line $s 88 340 88 384 $C.Line 2 | Out-Null
Add-Text $s 'Controller  →  Service  →  Repository/DAO  →  Database' 180 468 600 22 15 $C.Navy $true 2 | Out-Null

# 10. ERD
$s = New-Slide '프로젝트 ERD — 핵심 도메인' '06  ERD'
function EntityBox($slide,$name,$fields,$x,$y,$w=145,$h=92,$accent=$C.Blue){
    Add-Rect $slide $x $y $w $h $C.White $C.Line 5 | Out-Null
    Add-Rect $slide $x $y $w 28 $accent $accent 5 | Out-Null
    Add-Text $slide $name $x ($y+6) $w 18 11 $C.White $true 2 | Out-Null
    Add-Text $slide $fields ($x+10) ($y+37) ($w-20) ($h-43) 9.5 $C.Muted | Out-Null
}
EntityBox $s 'MEMBER' 'PK memberNo
userId · points' 64 162 145 94 $C.Blue
EntityBox $s 'CART' 'PK cartNo
FK memberNo · prodNo' 272 162 145 94 $C.Cyan
EntityBox $s 'PRODUCT' 'PK prodNo
FK shopNo · categoryId' 480 162 145 94 $C.Green
EntityBox $s 'PRODUCT_SKU' 'PK skuNo
FK prodNo · stock' 688 162 160 94 $C.Green
EntityBox $s 'ORDER' 'PK orderNo
FK memberNo · status' 168 344 155 94 $C.Orange
EntityBox $s 'ORDER_DETAILS' 'PK orderDetailNo
FK orderNo · productNo' 402 344 165 94 $C.Orange
EntityBox $s 'DELIVERIES' 'PK deliveryNo
FK orderNo · trackingNo' 650 344 165 94 $C.Red
Add-Line $s 209 209 272 209 $C.Cyan 2 $true | Out-Null
Add-Line $s 417 209 480 209 $C.Cyan 2 $true | Out-Null
Add-Line $s 625 209 688 209 $C.Cyan 2 $true | Out-Null
Add-Line $s 136 256 212 344 $C.Cyan 2 $true | Out-Null
Add-Line $s 323 391 402 391 $C.Orange 2 $true | Out-Null
Add-Line $s 567 391 650 391 $C.Orange 2 $true | Out-Null
Add-Line $s 552 256 485 344 $C.Green 2 $true | Out-Null
Add-Text $s '※ 발표용 핵심 관계만 단순화한 ERD' 665 470 205 18 9.5 $C.Muted $false 3 | Out-Null

# 11. Auth flow
$s = New-Slide '회원가입과 로그인' '07  DEMO'
$steps=@(
    @('1','가입 정보 입력','아이디 중복 확인 · 이메일 인증'),
    @('2','계정 저장','회원/판매자 유형에 맞는 데이터 생성'),
    @('3','인증','Form Login 또는 OAuth2 로그인'),
    @('4','권한 분기','USER · SELLER · ADMIN 화면 이동')
)
for($i=0;$i -lt 4;$i++){
    $x=58+$i*224
    if($i -lt 3){Add-Line $s ($x+66) 230 ($x+220) 230 $C.Cyan 2 $true | Out-Null}
    Add-Circle $s $steps[$i][0] $x 198 64 $(if($i -eq 3){$C.Orange}else{$C.Blue}) $C.White 17 | Out-Null
    Add-Text $s $steps[$i][1] ($x-25) 288 118 25 15 $C.Navy $true 2 | Out-Null
    Add-Text $s $steps[$i][2] ($x-45) 325 158 62 10.5 $C.Muted $false 2 | Out-Null
}
Add-Pill $s 'Spring Security' 250 420 160 $C.Navy $C.White
Add-Pill $s 'OAuth2 Client' 425 420 150
Add-Pill $s 'SMTP 인증' 590 420 120 $C.Pale $C.Green

# 12. Product
$s = New-Slide '상품 탐색과 상세 조회' '07  DEMO'
Add-Card $s '목록·카테고리' '카테고리 계층과 정렬 조건으로 상품 목록 제공' 54 154 255 126 $C.Cyan
Add-Card $s '검색·필터' '키워드, 가격, 판매량 등 복합 조건을 QueryDSL로 처리' 352 154 255 126 $C.Blue
Add-Card $s '상세·옵션' '상품 정보, 이미지, 리뷰, SKU 옵션과 재고 확인' 650 154 255 126 $C.Green
Add-Line $s 180 314 180 390 $C.Cyan 2 $true | Out-Null
Add-Line $s 480 314 480 390 $C.Cyan 2 $true | Out-Null
Add-Line $s 778 314 778 390 $C.Cyan 2 $true | Out-Null
Add-Pill $s 'Thymeleaf View' 103 405 155
Add-Pill $s 'QueryDSL' 421 405 118 $C.Pale $C.Blue
Add-Pill $s 'AWS S3 Image' 708 405 140 $C.Pale $C.Green

# 13. Checkout
$s = New-Slide '사용자 구매 시연 흐름' '07  DEMO'
$flow=@('로그인','상품 검색','상세/옵션','장바구니','주문/결제','주문 완료')
for($i=0;$i -lt $flow.Count;$i++){
    $x=48+$i*151
    if($i -lt 5){Add-Line $s ($x+58) 230 ($x+150) 230 $C.Cyan 2 $true | Out-Null}
    Add-Circle $s ($i+1).ToString() $x 202 56 $(if($i -eq 5){$C.Orange}else{$C.Blue}) $C.White 15 | Out-Null
    Add-Text $s $flow[$i] ($x-30) 285 116 48 11.5 $C.Ink $true 2 | Out-Null
}
Add-Rect $s 70 382 820 72 $C.Pale $C.Line 8 | Out-Null
Add-Text $s '사용자 시연' 92 399 140 22 14 $C.Blue $true | Out-Null
Add-Text $s '로그인부터 상품 탐색, 장바구니, 주문 완료까지 핵심 구매 흐름을 한 번에 시연' 238 399 620 40 11.5 $C.Muted | Out-Null

# 14. Admin
$s = New-Slide '관리자·판매자 운영 기능' '07  DEMO'
$items=@(
    @('상품/재고','등록 · 수정 · 삭제 · 옵션 · S3 이미지',$C.Green),
    @('주문/배송','주문상세 · 상태 변경 · 송장 등록',$C.Orange),
    @('회원/상점','등급 · 상태 · 판매자 상점 관리',$C.Blue),
    @('운영 설정','배너 · 카테고리 · 정책 · 버전',$C.Cyan),
    @('고객센터','공지 · FAQ · Q&A 답변',$C.Red),
    @('매출/쿠폰','통계 대시보드 · 쿠폰 등록/종료',$C.Navy)
)
for($i=0;$i -lt $items.Count;$i++){
    $col=$i%3;$row=[math]::Floor($i/3);$x=54+$col*299;$y=156+$row*148
    Add-Card $s $items[$i][0] $items[$i][1] $x $y 255 112 $items[$i][2]
}

# 15. Spring points
$s = New-Slide 'Spring 핵심 적용 내용' '07  DEMO'
$points=@(
    @('MVC 계층화','Controller · Service · Repository 책임 분리',$C.Blue),
    @('DI / IoC','생성자 주입으로 객체 결합도 완화',$C.Cyan),
    @('트랜잭션','주문 처리의 데이터 일관성 확보',$C.Orange),
    @('Security','URL·역할 기반 인증과 인가',$C.Red),
    @('데이터 접근','JPA + QueryDSL + MyBatis 병행',$C.Green),
    @('환경 설정','외부 비밀값과 운영 설정 분리',$C.Navy)
)
for($i=0;$i -lt $points.Count;$i++){
    $col=$i%2;$row=[math]::Floor($i/2);$x=60+$col*440;$y=150+$row*104
    Add-Circle $s ($i+1).ToString('00') $x ($y+9) 48 $points[$i][2] $C.White 10 | Out-Null
    Add-Text $s $points[$i][0] ($x+68) $y 310 24 15 $C.Navy $true | Out-Null
    Add-Text $s $points[$i][1] ($x+68) ($y+34) 310 36 11 $C.Muted | Out-Null
}

# 16. Problem solving
$s = New-Slide '구현 중 문제와 해결' '07  DEMO'
Add-Card $s '주문 데이터 불일치' '문제  재고·포인트·주문 저장이 따로 실패할 가능성

해결  @Transactional과 잠금 조회로 하나의 주문 단위 보장' 54 154 400 250 $C.Orange
Add-Card $s '역할별 접근 범위' '문제  사용자·판매자·관리자 화면의 권한 충돌

해결  Spring Security URL 규칙과 ROLE 기반 분기 적용' 506 154 400 250 $C.Blue
Add-Pill $s '데이터 일관성' 150 430 150 $C.Pale $C.Orange
Add-Pill $s '안전한 접근 제어' 650 430 160 $C.Pale $C.Blue

# 17. Demo flow
$s = New-Slide '프로젝트 시연 순서' '07  DEMO'
$demo=@(
    @('01','회원가입/로그인','이메일 인증 또는 SNS 로그인'),
    @('02','상품 탐색','검색 → 상세 → 옵션 선택'),
    @('03','구매','장바구니 → 주문 → 완료'),
    @('04','마이페이지','주문·포인트·쿠폰 확인'),
    @('05','관리자','상품/주문/배송 상태 처리')
)
for($i=0;$i -lt $demo.Count;$i++){
    $y=148+$i*67
    Add-Circle $s $demo[$i][0] 72 $y 44 $(if($i -eq 4){$C.Orange}else{$C.Blue}) $C.White 10 | Out-Null
    Add-Text $s $demo[$i][1] 136 ($y+3) 190 22 15 $C.Navy $true | Out-Null
    Add-Text $s $demo[$i][2] 340 ($y+4) 430 21 11.5 $C.Muted | Out-Null
    if($i -lt 4){Add-Line $s 94 ($y+44) 94 ($y+66) $C.Line 2 | Out-Null}
}
Add-Rect $s 790 164 104 262 $C.Navy $C.Navy 8 | Out-Null
Add-Text $s '시연
POINT' 790 190 104 58 14 $C.Cyan $true 2 | Out-Null
Add-Text $s '사용자 흐름과
관리자 처리가
하나로 연결되는지
확인' 802 280 80 95 11 $C.White $false 2 | Out-Null

# 18. Q&A
$s = $pres.Slides.Add($pres.Slides.Count + 1, 12)
$s.Background.Fill.ForeColor.RGB = $C.Navy
$s.Background.Fill.Solid()
$qaBg = Add-Rect $s 0 0 960 540 $C.Navy $C.Navy
$qaBg.Line.Visible = 0
Add-Rect $s 0 0 16 540 $C.Cyan $C.Cyan | Out-Null
Add-Text $s '08  Q&A' 72 92 180 24 12 $C.Cyan $true | Out-Null
Add-Text $s '질문 및 답변' 70 160 650 70 42 $C.White $true | Out-Null
Add-Text $s '감사합니다.' 72 252 300 36 22 (RGB 205 224 235) $false | Out-Null
Add-Line $s 72 330 888 330 (RGB 75 107 132) 1 | Out-Null
Add-Text $s 'K-Market  |  Spring Boot Team Project' 72 362 500 22 13 $C.Cyan $true | Out-Null
Add-Circle $s '?' 778 124 120 $C.Cyan $C.Navy 48 | Out-Null

# 발표 분량을 줄이기 위해 중복 설명 슬라이드를 제거한다.
# 최종 구성: 표지, 목차, 개요, 팀원, 일정, 아키텍처, 구조, ERD, 사용자 시연, 관리자 시연, Q&A
foreach($slideNo in @(17,16,15,12,11,8,4)) {
    $pres.Slides.Item($slideNo).Delete()
}

# 삭제 후 하단 페이지 번호를 다시 매긴다.
for($slideNo = 1; $slideNo -le $pres.Slides.Count; $slideNo++) {
    $slide = $pres.Slides.Item($slideNo)
    foreach($shape in @($slide.Shapes)) {
        if($shape.HasTextFrame -and $shape.TextFrame.HasText) {
            $value = $shape.TextFrame.TextRange.Text
            if($value -match '^K-Market\s+\|\s+\d{2}$') {
                $shape.TextFrame.TextRange.Text = 'K-Market  |  ' + $slideNo.ToString('00')
            }
        }
    }
}

$pptxPath = Join-Path $outDir 'K-Market_최종발표보고서.pptx'
$pdfPath = Join-Path $outDir 'K-Market_최종발표보고서.pdf'
$pres.SaveAs($pptxPath, 24)
$pres.SaveAs($pdfPath, 32)
$pres.Export($previewDir, 'PNG', 1600, 900)
$pres.Close()
$ppt.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($pres) | Out-Null
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($ppt) | Out-Null
[GC]::Collect(); [GC]::WaitForPendingFinalizers()

Write-Output "PPTX=$pptxPath"
Write-Output "PDF=$pdfPath"
Write-Output "PREVIEW=$previewDir"
