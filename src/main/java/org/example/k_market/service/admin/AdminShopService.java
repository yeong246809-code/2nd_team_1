package org.example.k_market.service.admin;

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
        LocalDateTime startDate = calculateStartDate(periodType);

        Page<Object[]> pageResult = salesRepository.findSalesStatusSummary(startDate, pageable);

        List<SalesStatusDTO> dtoList = pageResult.getContent().stream().map(row ->
                SalesStatusDTO.builder()
                        .shopNo(row[0] != null ? ((Number) row[0]).intValue() : 0) // int(11) 타입 맞춤 캐스팅
                        .storeName(row[1] != null ? row[1].toString() : "")
                        .bizNumber(row[2] != null ? row[2].toString() : "")
                        .orderCount(row[3] != null ? ((Number) row[3]).intValue() : 0)
                        .paymentCompleted(row[4] != null ? ((Number) row[4]).intValue() : 0)
                        .deliveryInProgress(row[5] != null ? ((Number) row[5]).intValue() : 0)
                        .deliveryCompleted(row[6] != null ? ((Number) row[6]).intValue() : 0)
                        .purchaseConfirmed(row[7] != null ? ((Number) row[7]).intValue() : 0)
                        .totalOrderAmount(row[8] != null ? ((Number) row[8]).longValue() : 0L)
                        .totalSalesAmount(row[9] != null ? ((Number) row[9]).longValue() : 0L)
                        .build()
        ).toList();

        return new PageImpl<>(dtoList, pageable, pageResult.getTotalElements());
    }

    private LocalDateTime calculateStartDate(String periodType) {
        LocalDateTime now = LocalDateTime.now();
        switch (periodType != null ? periodType : "daily") {
            case "weekly":  return now.minusWeeks(1);
            case "monthly": return now.minusMonths(1);
            case "daily":
            default:        return now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        }
    }
}