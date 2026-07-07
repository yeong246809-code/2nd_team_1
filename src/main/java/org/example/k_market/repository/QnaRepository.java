package org.example.k_market.repository;

import org.example.k_market.entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QnaRepository extends JpaRepository<Qna, Integer> {

    // 문의 목록 최신순 조회
    List<Qna> findAllByOrderByNoDesc();

    List<Qna> findAllByParentNoOrderByNoDesc(int parentNo);

    // 문의 답변 조회
    Optional<Qna> findByParentNo(int parentNo);



}