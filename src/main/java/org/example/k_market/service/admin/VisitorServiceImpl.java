package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.VisitorLog;
import org.example.k_market.repository.VisitorLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitorServiceImpl implements VisitorService {

    private final VisitorLogRepository visitorLogRepository;

    @Override
    @Transactional
    public void logVisitor(String sessionId, String ipAddress) {
        LocalDate today = LocalDate.now();

        // 오늘 날짜로 이미 기록된 세션이라면 저장하지 않고 무시 (중복 카운트 방지)
        if (!visitorLogRepository.existsByVisitDateAndSessionId(today, sessionId)) {
            VisitorLog log = VisitorLog.builder()
                    .visitDate(today)
                    .sessionId(sessionId)
                    .ipAddress(ipAddress)
                    .build();
            visitorLogRepository.save(log);
        }
    }

    @Override
    public long getVisitorCount(LocalDate date) {
        return visitorLogRepository.countByVisitDate(date);
    }
}