package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.DeliveryDTO;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.service.admin.DeliveryService;
import org.example.k_market.service.admin.OrderService;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.OrderDetailsRepository;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService; // 주문 관련 비즈니스 로직 처리
    private final DeliveryService deliveryService;
    private final ShopRepository shopRepository;
    private final OrderDetailsRepository orderDetailsRepository;

    // 1. 주문 목록 조회
    // 수정된 Controller 메서드
    @GetMapping("/list")
    public String list(Model model,
                       Authentication authentication,
                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword) {

        Page<OrderDTO> orderPage = isAdmin(authentication)
                ? orderService.findOrderList(pageable, searchType, keyword)
                : orderService.findSellerOrderList(currentShopNo(authentication), pageable, searchType, keyword);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        List<String> courierList = Arrays.asList("CJ대한통운", "우체국택배", "한진택배", "롯데택배", "로젠택배");

        model.addAttribute("courierList", courierList);

        return "admin/order/list";
    }

    // 2. 배송 상태 변경 처리 (Ajax 요청 대응)
    @PostMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @RequestParam("orderNo") int orderNo,
            @RequestParam("status") String status,
            Authentication authentication) {

        if (isAdmin(authentication)) orderService.updateOrderStatus(orderNo, status);
        else orderService.updateSellerOrderStatus(currentShopNo(authentication), orderNo, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{orderNo}")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable int orderNo, Authentication authentication) {
        OrderDTO orderDetail = isAdmin(authentication)
                ? orderService.findOrderDetail(orderNo)
                : orderService.findSellerOrderDetail(currentShopNo(authentication), orderNo);
        return ResponseEntity.ok(orderDetail);
    }

    @PostMapping("/delivery/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerDelivery(@RequestBody DeliveryDTO deliveryDTO,
                                                                 Authentication authentication) {
        try {
            Long sellerShopNo = null;
            if (!isAdmin(authentication)) {
                long shopNo = currentShopNo(authentication);
                sellerShopNo = shopNo;
                var detail = orderDetailsRepository
                        .findByOrderNoAndShopNo(deliveryDTO.getOrderNo(), shopNo)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                                "다른 판매자의 주문입니다."));
                deliveryDTO.setOrderNo(detail.getOrderNo());
                deliveryDTO.setOrderDetailNo(detail.getOrderDetailNo());
            }
            deliveryService.registerDelivery(deliveryDTO);
            if (sellerShopNo != null) {
                orderService.updateSellerOrderStatus(
                        sellerShopNo, Math.toIntExact(deliveryDTO.getOrderNo()), "배송중");
            } else {
                orderService.updateOrderStatus(Math.toIntExact(deliveryDTO.getOrderNo()), "배송중");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/delivery")
    public String deliveryList(@PageableDefault(size = 10) Pageable pageable, Model model,
                               Authentication authentication) {
        Page<DeliveryDTO> page = isAdmin(authentication)
                ? deliveryService.getDeliveryList(pageable)
                : deliveryService.getSellerDeliveryList(currentShopNo(authentication), pageable);

        // 만약 서비스에서 null을 반환한다면 빈 페이지 객체를 넘겨줍니다.
        if (page == null) {
            page = Page.empty();
        }

        model.addAttribute("deliveryPage", page != null ? page : Page.empty());
        return "admin/order/delivery";
    }

    @GetMapping("/delivery/detail/{trackingNumber}")
    @ResponseBody // JSON 반환
    public ResponseEntity<DeliveryDTO> getDeliveryDetail(@PathVariable String trackingNumber,
                                                          Authentication authentication) {
        // 1. 송장번호로 배송 정보 조회
        DeliveryDTO detail = deliveryService.getDetailByTrackingNumber(trackingNumber);
        if (!isAdmin(authentication) && !orderDetailsRepository.existsByOrderDetailNoAndShopNo(
                detail.getOrderDetailNo(), currentShopNo(authentication))) {
            throw new org.springframework.security.access.AccessDeniedException("다른 판매자의 배송입니다.");
        }
        return ResponseEntity.ok(detail);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private long currentShopNo(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails userDetails)) {
            throw new org.springframework.security.access.AccessDeniedException("판매자 정보를 확인할 수 없습니다.");
        }
        Shop shop = shopRepository.findById(userDetails.getUser().getMemberNo())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                        "판매자 상점 정보를 확인할 수 없습니다."));
        if (shop.getShopNo() == 0) {
            throw new org.springframework.security.access.AccessDeniedException("판매자 상점 번호가 없습니다.");
        }
        return shop.getShopNo();
    }

}
