package org.example.k_market.service;

import org.example.k_market.dto.SalesStatusDTO;
import org.example.k_market.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AdminShopService {

    private final SalesRepository salesRepository;

    public Page<SalesStatusDTO> getSalesStatusList(String periodType, Pageable pageable) {

        // 핵심: 필터 조건에 맞는 시작 날짜를 계산합니다.
        LocalDateTime startDate = calculateStartDate(periodType);

        // 계산된 날짜를 JPA Repository의 네이티브 쿼리 파라미터로 주입합니다.
        Page<Object[]> pageResult = salesRepository.findSalesStatusSummary(startDate, pageable);

        // 이하 Object[] -> DTO 매핑 로직 (기존 코드 그대로 유지)
        List<SalesStatusDTO> dtoList = pageResult.getContent().stream().map(row -> {
            return SalesStatusDTO.builder()
                    .shopNo(row[0] != null ? ((Number) row[0]).longValue() : 0L)
                    .storeName(row[1] != null ? row[1].toString() : "")
                    .bizNumber(row[2] != null ? row[2].toString() : "")
                    .orderCount(row[3] != null ? ((Number) row[3]).intValue() : 0)
                    .paymentCompleted(row[4] != null ? ((Number) row[4]).intValue() : 0)
                    .deliveryInProgress(row[5] != null ? ((Number) row[5]).intValue() : 0)
                    .deliveryCompleted(row[6] != null ? ((Number) row[6]).intValue() : 0)
                    .purchaseConfirmed(row[7] != null ? ((Number) row[7]).intValue() : 0)
                    .totalOrderAmount(row[8] != null ? ((Number) row[8]).longValue() : 0L)
                    .totalSalesAmount(row[9] != null ? ((Number) row[9]).longValue() : 0L)
                    .build();
        }).toList();

        return new PageImpl<>(dtoList, pageable, pageResult.getTotalElements());
    }

    /**
     * 주별, 월별, 일별에 따라 시작 날짜를 동적으로 다르게 연산하는 메서드
     */
    private LocalDateTime calculateStartDate(String periodType) {
        LocalDateTime now = LocalDateTime.now();

        switch (periodType) {
            case "weekly":
                // 주별 선택 시: 정확히 1주일 전(7일 전)부터 현재까지의 주문 데이터 집계
                return now.minusWeeks(1);
            case "monthly":
                // 월별 선택 시: 정확히 1달 전부터 현재까지의 주문 데이터 집계
                return now.minusMonths(1);
            case "daily":
            default:
                // 일별 선택 시: 오늘 자정(00:00:00)부터 현재까지 발생한 주문 데이터 집계
                return now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
    }
}