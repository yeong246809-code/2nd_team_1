package org.example.k_market.service.admin;

import org.example.k_market.dto.CouponDTO;
import org.example.k_market.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCouponService {
    static Object getCouponList(java.awt.print.Pageable pageable) {
        return null;
    }

    Page<CouponDTO> getCouponList(Pageable pageable);
    Coupon getCouponDetail(String couponNo);

    void terminateCoupon(String couponNo);


    void registerCoupon(CouponDTO dto);
}
