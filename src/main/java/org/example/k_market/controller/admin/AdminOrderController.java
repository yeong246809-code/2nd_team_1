package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.DeliveriesDTO;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.service.admin.DeliveryService;
import org.example.k_market.service.admin.OrderService;
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

    // 1. 주문 목록 조회
    // 수정된 Controller 메서드
    @GetMapping("/list")
    public String list(Model model,
                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword) {

        Page<OrderDTO> orderPage = orderService.findOrderList(pageable, searchType, keyword);

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
            @RequestParam("status") String status) {

        orderService.updateOrderStatus(orderNo, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/detail/{orderNo}")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable int orderNo) {
        OrderDTO orderDetail = orderService.findOrderDetail(orderNo);
        return ResponseEntity.ok(orderDetail);
    }

    @PostMapping("/delivery/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> registerDelivery(@RequestBody DeliveriesDTO deliveryDTO) {
        try {
            deliveryService.registerDelivery(deliveryDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
