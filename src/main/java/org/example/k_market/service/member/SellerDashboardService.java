package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.SellerDashboardDTO;
import org.example.k_market.entity.Category;
import org.example.k_market.entity.Order;
import org.example.k_market.entity.OrderDetails;
import org.example.k_market.entity.Product;
import org.example.k_market.repository.CategoryRepository;
import org.example.k_market.repository.OrderDetailsRepository;
import org.example.k_market.repository.OrderRepository;
import org.example.k_market.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerDashboardService {

    private final OrderDetailsRepository orderDetailsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public SellerDashboardDTO getDashboard(long shopNo) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusDays(4).atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();
        List<OrderDetails> details = orderDetailsRepository.findSellerDetailsBetween(shopNo, from, to);
        List<OrderDetails> currentDetails = orderDetailsRepository.findByShopNo(shopNo);

        Map<Long, Order> orders = new HashMap<>();
        for (OrderDetails detail : details) {
            orders.computeIfAbsent(detail.getOrderNo(), no ->
                    orderRepository.findById(Math.toIntExact(no)).orElse(null));
        }

        long pending = countStatus(currentDetails, "입금대기", "결제대기");
        long preparing = countStatus(currentDetails, "배송준비", "결제완료");
        long cancel = countStatus(currentDetails, "취소요청", "주문취소");
        long exchange = countStatus(currentDetails, "교환요청");
        long returned = countStatus(currentDetails, "반품요청");

        List<SellerDashboardDTO.DailySummary> daily = new ArrayList<>();
        for (int i = 4; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<OrderDetails> dayDetails = details.stream()
                    .filter(detail -> isOnDate(orders.get(detail.getOrderNo()), date))
                    .toList();
            long orderCount = distinctOrderCount(dayDetails);
            long payCount = dayDetails.stream().filter(detail -> !isStatus(detail, "취소", "반품")).count();
            long cancelCount = dayDetails.stream().filter(detail -> isStatus(detail, "취소")).count();
            daily.add(new SellerDashboardDTO.DailySummary(
                    date.format(DateTimeFormatter.ofPattern("MM-dd")), orderCount, payCount, cancelCount));
        }

        Map<String, Long> categoryTotals = new HashMap<>();
        for (OrderDetails detail : details) {
            if (isStatus(detail, "취소", "반품")) continue;
            Product product = productRepository.findById(detail.getProductNo()).orElse(null);
            if (product == null) continue;
            String categoryName = categoryRepository.findById(product.getCateNo())
                    .map(Category::getName)
                    .orElse("기타");
            categoryTotals.merge(categoryName, salesAmount(detail), Long::sum);
        }
        long totalSales = categoryTotals.values().stream().mapToLong(Long::longValue).sum();
        List<SellerDashboardDTO.CategorySales> topSales = categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(4)
                .map(entry -> new SellerDashboardDTO.CategorySales(
                        entry.getKey(), entry.getValue(),
                        totalSales == 0 ? 0 : (int) Math.round(entry.getValue() * 100.0 / totalSales)))
                .toList();

        return SellerDashboardDTO.builder()
                .pendingDeposit(pending)
                .preparingDelivery(preparing)
                .cancelRequest(cancel)
                .exchangeRequest(exchange)
                .returnRequest(returned)
                .dailySummaryList(daily)
                .topSalesList(topSales)
                .build();
    }

    private long countStatus(List<OrderDetails> details, String... statuses) {
        return details.stream().filter(detail -> isStatus(detail, statuses)).count();
    }

    private boolean isStatus(OrderDetails detail, String... statuses) {
        String current = detail.getStatus();
        if (current == null) return false;
        for (String status : statuses) {
            if (current.contains(status)) return true;
        }
        return false;
    }

    private boolean isOnDate(Order order, LocalDate date) {
        return order != null && order.getCreatedAt() != null
                && order.getCreatedAt().toLocalDate().equals(date);
    }

    private long distinctOrderCount(List<OrderDetails> details) {
        Set<Long> orderNos = new LinkedHashSet<>();
        details.forEach(detail -> orderNos.add(detail.getOrderNo()));
        return orderNos.size();
    }

    private long salesAmount(OrderDetails detail) {
        return Math.max(0L, (long) detail.getPrice() * detail.getQuantity() - detail.getDiscountPrice());
    }
}
