package org.example.k_market.repository;

import org.example.k_market.entity.VisitorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {

    // [통계용] 특정 날짜의 총 방문자 수 조회
    long countByVisitDate(LocalDate visitDate);

    // [중복 방지용] 오늘 날짜에 해당 세션 ID로 접속한 기록이 있는지 확인
    boolean existsByVisitDateAndSessionId(LocalDate visitDate, String sessionId);
}