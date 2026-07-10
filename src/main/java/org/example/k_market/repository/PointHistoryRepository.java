package org.example.k_market.repository;

import org.example.k_market.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    // memberNo 리스트에 해당하는 포인트 내역 조회 (검색용)
    Page<PointHistory> findByMemberNoIn(List<Integer> memberNos, Pageable pageable);
}