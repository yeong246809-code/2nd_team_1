package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.OrderDTO;
import org.example.k_market.service.admin.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService; // 주문 관련 비즈니스 로직 처리

    // 1. 주문 목록 조회
    @GetMapping("/list")
    public String list(Model model,
                       @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
                       @RequestParam(required = false) String searchType,
                       @RequestParam(required = false) String keyword, Pageable pageable) {

        Page<OrderDTO> orderPage = orderService.findOrderList(pageable, searchType, keyword);

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "admin/order/list";
    }

    // 2. 배송 상태 변경 처리 (Ajax 요청 대응)
    @PostMapping("/updateStatus")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(@RequestBody Map<String, String> request) {
        String orderNo = request.get("orderNo");
        String status = request.get("status");

        boolean success = orderService.updateOrderStatus(orderNo, status);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "상태가 변경되었습니다." : "변경에 실패했습니다.");

        return ResponseEntity.ok(response);
    }
}
