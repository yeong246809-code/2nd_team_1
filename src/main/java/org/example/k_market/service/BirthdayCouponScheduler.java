package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BirthdayCouponScheduler {
    private final CouponIssuanceService couponIssuanceService;

    @EventListener(ApplicationReadyEvent.class)
    public void issueOnStartup() {
        issueBirthdayCoupons();
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void issueBirthdayCoupons() {
        int count = couponIssuanceService.issueTodayBirthdayCoupons();
        if (count > 0) log.info("오늘 생일인 일반회원 {}명에게 생일축하 쿠폰을 지급했습니다.", count);
    }
}
