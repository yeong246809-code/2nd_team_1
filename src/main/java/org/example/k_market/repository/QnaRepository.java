package org.example.k_market.repository;

import org.apache.ibatis.annotations.Param;
import org.example.k_market.entity.Qna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Integer> {

    // 사용자 문의 목록: 원글(parentNo=0)만 페이지 단위로 조회
    Page<Qna> findAllByParentNo(Integer parentNo, Pageable pageable);

    // 기존 목록 조회가 필요한 다른 화면에서 사용
    List<Qna> findAllByOrderByNoDesc();

    List<Qna> findAllByParentNoOrderByNoDesc(int parentNo);

    // 문의 답변 조회
    Optional<Qna> findByParentNo(int parentNo);

    // 고객센터 메인 최신 문의 5개
    List<Qna> findTop5ByParentNoOrderByNoDesc(int parentNo);

    // 상품별 문의 원글 조회
    List<Qna> findByProdNoAndParentNoOrderByNoDesc(Long prodNo, int parentNo);

    Page<Qna> findByMemberNoAndParentNoOrderByNoDesc(Integer memberNo, Integer parentNo, Pageable pageable);

    long countByMemberNoAndParentNo(Integer memberNo, Integer parentNo);

    @Query("SELECT COUNT(q) FROM Qna q WHERE q.createdAt >= :start AND q.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
