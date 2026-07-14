package org.example.k_market.service.admin;

import java.time.LocalDate;

public interface VisitorService {
    void logVisitor(String sessionId, String ipAddress);
    long getVisitorCount(LocalDate date);
}