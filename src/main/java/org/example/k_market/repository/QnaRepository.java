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

    // 고객센터 메인에 출력할 문의글 최신 5개 조회
    // parentNo가 0인 원글만 가져오고, 답변글은 제외한다.
    List<Qna> findTop5ByParentNoOrderByNoDesc(int parentNo);

    // 신규 추가: 특정 상품에 대한 문의 원글만 최신순 조회 (parentNo=0 -> 답변글 제외)
    List<Qna> findByProductNoAndParentNoOrderByNoDesc(Long productNo, int parentNo);

}