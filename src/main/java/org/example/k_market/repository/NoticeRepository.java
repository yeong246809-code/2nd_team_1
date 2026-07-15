package org.example.k_market.repository;

import org.example.k_market.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



// 공지사항 JPA Repository
// DAO 없이 JpaRepository로 DB 조회/저장 처리
public interface NoticeRepository extends JpaRepository<Notice, Integer> {

    // 공지사항 목록 전체 조회
    // no 기준 내림차순으로 전체 공지사항을 가져온다.
    List<Notice> findAllByOrderByNoDesc();

    // 고객센터 메인에 출력할 최신 공지사항 5개 조회
    // no 기준 내림차순으로 최근 글 5개만 가져온다.
    List<Notice> findTop5ByOrderByNoDesc();



    // 공지사항 유형별 최신순 조회
    List<Notice> findAllByTypeOrderByNoDesc(String type);

}