package org.example.k_market.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SellerDashboardDTO {
    private long pendingDeposit;
    private long preparingDelivery;
    private long cancelRequest;
    private long exchangeRequest;
    private long returnRequest;
    private List<DailySummary> dailySummaryList;
    private List<CategorySales> topSalesList;

    public record DailySummary(String date, long orderCount, long payCount, long cancelCount) {}

    public record CategorySales(String categoryName, long totalSales, int percentage) {}
}
