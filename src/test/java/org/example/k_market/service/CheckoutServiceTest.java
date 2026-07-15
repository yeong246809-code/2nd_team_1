package org.example.k_market.service;

import org.example.k_market.dto.CheckoutRequest;
import org.example.k_market.entity.*;
import org.example.k_market.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CheckoutServiceTest {
    @Autowired CheckoutService checkoutService;
    @Autowired MemberRepository memberRepository;
    @Autowired ProductRepository productRepository;
    @Autowired ProductSkuRepository productSkuRepository;
    @Autowired CartRepository cartRepository;
    @Autowired OrderDetailsRepository orderDetailsRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderDeliveryRepository orderDeliveryRepository;
    @Autowired UsersRepository usersRepository;
    @Autowired CouponRepository couponRepository;
    @Autowired CouponDetailsRepository couponDetailsRepository;
    @Autowired CouponIssuanceService couponIssuanceService;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void cartOrderUsesPointsDecreasesStockAndDeletesCart() {
        ensureActiveShop(1);
        Member member = createMember("checkout-cart", "주문테스트", 6000);
        Product product = productRepository.save(Product.builder()
                .shopNo(1).name("카트 주문 상품").price(10000).discountRate(10)
                .shippingFee(2500).rewardPoints(100).stockQuantity(5).salesCount(0)
                .createdAt(LocalDateTime.now()).build());
        Cart cart = cartRepository.save(Cart.builder()
                .memberNo(member.getMemberNo()).prodNo(product.getProdNo()).quantity(2)
                .createdAt(LocalDateTime.now()).build());

        CheckoutRequest request = baseRequest();
        request.setCartNos(List.of(cart.getCartNo()));
        request.setUsedPoints(1000);

        CheckoutService.CheckoutResult result = checkoutService.placeOrder(member.getMemberNo(), request);

        Order savedOrder = orderRepository.findById(result.orderNo()).orElseThrow();
        List<OrderDetails> details = orderDetailsRepository.findByOrderNo(result.orderNo());
        assertThat(savedOrder.getTotalPaymentPrice()).isEqualTo(19500);
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getSkuNo()).isNull();
        assertThat(details.get(0).getRewardPoints()).isEqualTo(200);
        assertThat(cartRepository.findById(cart.getCartNo())).isEmpty();
        assertThat(productRepository.findById(product.getProdNo()).orElseThrow().getStockQuantity()).isEqualTo(3);
        assertThat(memberRepository.findById(member.getMemberNo()).orElseThrow().getPoints()).isEqualTo(5000);
    }

    @Test
    void directOrderPersistsSkuAndDecreasesSkuStock() {
        ensureActiveShop(1);
        Member member = createMember("checkout-sku", "SKU테스트", 0);
        Product product = productRepository.save(Product.builder()
                .shopNo(1).name("옵션 주문 상품").price(10000).discountRate(0)
                .shippingFee(0).rewardPoints(0).stockQuantity(0).salesCount(0)
                .createdAt(LocalDateTime.now()).build());
        ProductSku sku = productSkuRepository.save(ProductSku.builder()
                .prodNo(product.getProdNo()).skuName("블랙/L").price(500).stock(3).build());

        CheckoutRequest request = baseRequest();
        request.setDirectProdNo(product.getProdNo());
        request.setDirectSkuNo(sku.getSkuNo());
        request.setDirectQuantity(2);

        CheckoutService.CheckoutResult result = checkoutService.placeOrder(member.getMemberNo(), request);

        OrderDetails detail = orderDetailsRepository.findByOrderNo(result.orderNo()).get(0);
        assertThat(detail.getSkuNo()).isEqualTo(sku.getSkuNo());
        assertThat(detail.getPrice()).isEqualTo(10500);
        assertThat(result.totalPaymentPrice()).isEqualTo(21000);
        assertThat(productSkuRepository.findById(sku.getSkuNo()).orElseThrow().getStock()).isEqualTo(1);
    }

    @Test
    void sameSellerUsesOnlyHighestShippingFeeAndCanSplitDelivery() {
        ensureActiveShop(7);
        Member member = createMember("checkout-split", "분할배송", 0);
        Product first = productRepository.save(Product.builder()
                .shopNo(7).name("첫 상품").price(10000).discountRate(0)
                .shippingFee(3000).rewardPoints(0).stockQuantity(5).salesCount(0)
                .createdAt(LocalDateTime.now()).build());
        Product second = productRepository.save(Product.builder()
                .shopNo(7).name("둘째 상품").price(20000).discountRate(0)
                .shippingFee(5000).rewardPoints(0).stockQuantity(5).salesCount(0)
                .createdAt(LocalDateTime.now()).build());
        Cart firstCart = cartRepository.save(Cart.builder()
                .memberNo(member.getMemberNo()).prodNo(first.getProdNo()).quantity(2)
                .createdAt(LocalDateTime.now()).build());
        Cart secondCart = cartRepository.save(Cart.builder()
                .memberNo(member.getMemberNo()).prodNo(second.getProdNo()).quantity(1)
                .createdAt(LocalDateTime.now()).build());

        CheckoutRequest request = baseRequest();
        request.setCartNos(List.of(firstCart.getCartNo(), secondCart.getCartNo()));
        request.setOrdererName("주문자");
        request.setOrdererPhone("010-9999-9999");
        request.setOrdererBaseAddress("부산광역시");
        request.setShipments(List.of(
                shipment("배송지A", "서울시", List.of(
                        shipmentItem("cart:" + firstCart.getCartNo(), 1),
                        shipmentItem("cart:" + secondCart.getCartNo(), 1))),
                shipment("배송지B", "대전시", List.of(
                        shipmentItem("cart:" + firstCart.getCartNo(), 1)))))
        ;

        CheckoutService.CheckoutResult result = checkoutService.placeOrder(member.getMemberNo(), request);

        Order savedOrder = orderRepository.findById(result.orderNo()).orElseThrow();
        assertThat(savedOrder.getTotalShippingFee()).isEqualTo(5000);
        assertThat(savedOrder.getTotalPaymentPrice()).isEqualTo(45000);
        assertThat(orderDetailsRepository.findByOrderNo(result.orderNo()))
                .extracting(OrderDetails::getShippingFee)
                .containsExactlyInAnyOrder(0, 5000);
        assertThat(orderDeliveryRepository.findByOrderNoOrderByShipmentIndexAscOrderDeliveryNoAsc(result.orderNo()))
                .hasSize(3)
                .extracting(OrderDelivery::getRecipientName)
                .containsExactly("배송지A", "배송지A", "배송지B");
    }

    @Test
    void stoppedShopProductIsNotVisible() {
        jdbcTemplate.update("INSERT INTO shop (member_no, shop_no, name, status) VALUES (?, ?, ?, 'STOPPED')",
                9099, 99, "중지상점");
        Product product = productRepository.save(Product.builder()
                .shopNo(99).name("노출되면 안 되는 상품").price(10000).discountRate(0)
                .shippingFee(0).rewardPoints(0).stockQuantity(1).salesCount(0)
                .createdAt(LocalDateTime.now()).build());

        assertThat(productRepository.findVisibleById(product.getProdNo())).isEmpty();
        assertThat(productRepository.findAllVisible()).doesNotContain(product);
    }

    @Test
    void ownedCouponDiscountsOrderAndBecomesUsed() {
        ensureActiveShop(12);
        Member member = createMember("checkout-coupon", "쿠폰테스트", 0);
        Product product = productRepository.save(Product.builder()
                .shopNo(12).name("쿠폰 주문 상품").price(20000).discountRate(0)
                .shippingFee(3000).rewardPoints(0).stockQuantity(2).salesCount(0)
                .createdAt(LocalDateTime.now()).build());
        Coupon coupon = couponRepository.save(Coupon.builder()
                .issuerName("최고관리자").couponType(CouponIssuanceService.ORDER_DISCOUNT)
                .name("주문 5천원 할인").benefitType("AMOUNT").benefitValue(5000)
                .status("ACTIVE").createdAt(LocalDateTime.now()).build());
        CouponDetails issued = couponDetailsRepository.save(CouponDetails.builder()
                .couponNo(coupon.getCouponNo()).memberNo(member.getMemberNo()).isUsed("N")
                .status("사용가능").issuedAt(LocalDateTime.now()).build());

        CheckoutRequest request = baseRequest();
        request.setDirectProdNo(product.getProdNo());
        request.setDirectQuantity(1);
        request.setCouponDetailNo(issued.getCouponDetailNo());

        CheckoutService.CheckoutResult result = checkoutService.placeOrder(member.getMemberNo(), request);

        Order savedOrder = orderRepository.findById(result.orderNo()).orElseThrow();
        assertThat(savedOrder.getTotalDiscountPrice()).isEqualTo(5000);
        assertThat(savedOrder.getTotalPaymentPrice()).isEqualTo(18000);
        assertThat(couponDetailsRepository.findById(issued.getCouponDetailNo()).orElseThrow().getIsUsed())
                .isEqualTo("Y");
    }

    @Test
    void bannerCouponCanOnlyBeClaimedOnce() {
        Member member = createMember("banner-coupon", "배너쿠폰", 0);
        Coupon coupon = couponRepository.save(Coupon.builder()
                .issuerName("최고관리자").couponType(CouponIssuanceService.ORDER_DISCOUNT)
                .name("생성기념 쿠폰").benefitType("AMOUNT").benefitValue(1000)
                .status("ACTIVE").createdAt(LocalDateTime.now()).build());

        couponIssuanceService.claimByCouponNo(coupon.getCouponNo(), member.getMemberNo());

        assertThatThrownBy(() -> couponIssuanceService.claimByCouponNo(
                coupon.getCouponNo(), member.getMemberNo()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 발급");
        assertThat(couponDetailsRepository.countByCouponNo(coupon.getCouponNo())).isEqualTo(1);
    }

    @Test
    void freeShippingCouponRemovesGroupedShippingFee() {
        ensureActiveShop(13);
        Member member = createMember("shipping-coupon", "배송쿠폰", 0);
        Product product = productRepository.save(Product.builder()
                .shopNo(13).name("배송비 쿠폰 상품").price(10000).discountRate(0)
                .shippingFee(3500).rewardPoints(0).stockQuantity(2).salesCount(0)
                .createdAt(LocalDateTime.now()).build());
        Coupon coupon = couponRepository.save(Coupon.builder()
                .issuerName("최고관리자").couponType(CouponIssuanceService.FREE_SHIPPING)
                .name("배송비무료 확인용 쿠폰").benefitType("FREE_SHIPPING").benefitValue(0)
                .status("ACTIVE").createdAt(LocalDateTime.now()).build());
        CouponDetails issued = couponDetailsRepository.save(CouponDetails.builder()
                .couponNo(coupon.getCouponNo()).memberNo(member.getMemberNo()).isUsed("N")
                .status("사용가능").issuedAt(LocalDateTime.now()).build());

        CheckoutRequest request = baseRequest();
        request.setDirectProdNo(product.getProdNo());
        request.setDirectQuantity(1);
        request.setCouponDetailNo(issued.getCouponDetailNo());

        CheckoutService.CheckoutResult result = checkoutService.placeOrder(member.getMemberNo(), request);

        Order savedOrder = orderRepository.findById(result.orderNo()).orElseThrow();
        assertThat(savedOrder.getTotalShippingFee()).isZero();
        assertThat(savedOrder.getTotalPaymentPrice()).isEqualTo(10000);
        assertThat(orderDetailsRepository.findByOrderNo(result.orderNo()).get(0).getShippingFee()).isZero();
    }

    private CheckoutRequest.Shipment shipment(String name, String address,
                                               List<CheckoutRequest.ShipmentItem> items) {
        CheckoutRequest.Shipment shipment = new CheckoutRequest.Shipment();
        shipment.setRecipientName(name);
        shipment.setRecipientPhone("010-1111-2222");
        shipment.setBaseAddress(address);
        shipment.setItems(items);
        return shipment;
    }

    private CheckoutRequest.ShipmentItem shipmentItem(String itemKey, int quantity) {
        CheckoutRequest.ShipmentItem item = new CheckoutRequest.ShipmentItem();
        item.setItemKey(itemKey);
        item.setQuantity(quantity);
        return item;
    }

    private CheckoutRequest baseRequest() {
        CheckoutRequest request = new CheckoutRequest();
        request.setRecipientName("홍길동");
        request.setRecipientPhone("010-1234-5678");
        request.setZipCode("12345");
        request.setBaseAddress("부산광역시 부산진구");
        request.setDetailAddress("101호");
        request.setMemo("문 앞");
        request.setPaymentMethod("CARD");
        request.setUsedPoints(0);
        return request;
    }

    private void ensureActiveShop(int shopNo) {
        jdbcTemplate.update("INSERT INTO shop (member_no, shop_no, name, status) VALUES (?, ?, ?, 'ACTIVE')",
                9000 + shopNo, shopNo, "테스트상점" + shopNo);
    }

    private Member createMember(String id, String name, int points) {
        Users user = usersRepository.saveAndFlush(Users.builder()
                .id(id).pass("test").role("USER").build());
        return memberRepository.save(Member.builder()
                .memberNo(user.getMemberNo()).name(name).points(points).build());
    }
}
