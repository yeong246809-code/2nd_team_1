package org.example.k_market.repository;

import org.example.k_market.entity.Banner;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    @Query("""
            SELECT b
            FROM Banner b
            WHERE b.position = :position
              AND (b.status = '활성' OR UPPER(b.status) = 'ACTIVE')
              AND (b.startDate IS NULL OR b.startDate <= :today)
              AND (b.endDate IS NULL OR b.endDate >= :today)
              AND (b.startTime IS NULL OR b.startTime <= :nowTime)
              AND (b.endTime IS NULL OR b.endTime >= :nowTime)
              AND b.fileUrl_stored IS NOT NULL
              AND b.fileUrl_stored <> ''
            ORDER BY b.id DESC
            """)
    List<Banner> findDisplayableByPosition(
            @Param("position") String position,
            @Param("today") LocalDate today,
            @Param("nowTime") LocalTime nowTime,
            Pageable pageable);
}
