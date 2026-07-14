package org.example.k_market.service;

import org.example.k_market.dto.CheckoutRequest;
import org.example.k_market.entity.*;
import org.example.k_market.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    @Autowired UsersRepository usersRepository;

    @Test
    void cartOrderUsesPointsDecreasesStockAndDeletesCart() {
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

    private Member createMember(String id, String name, int points) {
        Users user = usersRepository.saveAndFlush(Users.builder()
                .id(id).pass("test").role("USER").build());
        return memberRepository.save(Member.builder()
                .memberNo(user.getMemberNo()).name(name).points(points).build());
    }
}
