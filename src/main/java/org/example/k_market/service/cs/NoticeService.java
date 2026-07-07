package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Notice;
import org.example.k_market.repository.NoticeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 공지사항 목록 조회
    public List<Notice> findAll() {
        return noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "no"));
    }

    // 공지사항 상세 조회
    public Notice findById(int no) {
        return noticeRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
    }
}