-- 기존 주문의 orderName을 주문자 실제 이름으로 통일한다.
-- 우선순위: 저장된 주문자명 > 회원명 > 수령인명 > 기존 주문명
UPDATE `order` o
LEFT JOIN member m ON m.memberNo = o.memberNo
SET o.ordererName = COALESCE(
        NULLIF(TRIM(o.ordererName), ''),
        NULLIF(TRIM(m.name), ''),
        NULLIF(TRIM(o.recipientName), ''),
        o.orderName
    ),
    o.orderName = COALESCE(
        NULLIF(TRIM(o.ordererName), ''),
        NULLIF(TRIM(m.name), ''),
        NULLIF(TRIM(o.recipientName), ''),
        o.orderName
    );
