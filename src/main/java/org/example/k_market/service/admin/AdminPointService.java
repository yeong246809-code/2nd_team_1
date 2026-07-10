package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.AdminPointListDTO;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.PointHistory;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.PointHistoryRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminPointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;
    private final UsersRepository usersRepository; // 아이디 조회를 위해 추가

    // 1. 포인트 목록 조회 및 검색 (추가된 부분!)
    @Transactional(readOnly = true)
    public Page<AdminPointListDTO> getPoints(String searchType, String keyword, Pageable pageable) {
        Page<PointHistory> pointPage;

        // 검색어가 없을 때 전체 조회
        if (keyword == null || keyword.trim().isEmpty()) {
            pointPage = pointHistoryRepository.findAll(pageable);
        } else {
            // 검색어가 있을 때 (아이디 또는 이름으로 회원 번호 먼저 추출)
            List<Integer> memberNos = new ArrayList<>();

            if ("name".equals(searchType)) {
                memberNos = memberRepository.findByNameContaining(keyword, Pageable.unpaged())
                        .getContent().stream().map(Member::getMemberNo).collect(Collectors.toList());
            } else if ("id".equals(searchType)) {
                memberNos = usersRepository.findByIdContaining(keyword, Pageable.unpaged())
                        .getContent().stream().map(Users::getMemberNo).collect(Collectors.toList());
            }

            // 검색된 회원이 없으면 빈 페이지 반환
            if (memberNos.isEmpty()) {
                return Page.empty(pageable);
            }
            // 찾은 회원 번호들로 포인트 내역 조회
            pointPage = pointHistoryRepository.findByMemberNoIn(memberNos, pageable);
        }

        // Entity -> DTO 변환 (화면 출력용 데이터 맵핑)
        List<AdminPointListDTO> dtoList = pointPage.getContent().stream().map(history -> {
            Member member = memberRepository.findById(history.getMemberNo()).orElse(null);
            Users user = usersRepository.findByMemberNo(history.getMemberNo()).orElse(null);

            return AdminPointListDTO.builder()
                    .pointNo(history.getPointNo())
                    .id(user != null ? user.getId() : "탈퇴회원")
                    .name(member != null ? member.getName() : "알수없음")
                    .amount(history.getAmount())
                    .remainedAmount(history.getRemainedAmount())
                    .description(history.getDescription())
                    .createdAt(history.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, pointPage.getTotalElements());
    }

    // 2. 선택된 포인트 내역 삭제 및 회수 처리 (기존 제공했던 부분)
    public void deletePointHistories(List<Long> pointNos) {
        if (pointNos == null || pointNos.isEmpty()) return;

        List<PointHistory> histories = pointHistoryRepository.findAllById(pointNos);

        for (PointHistory history : histories) {
            Member member = memberRepository.findById(history.getMemberNo()).orElse(null);
            if (member != null) {
                int rolledBackPoints = member.getPoints() - history.getAmount();
                Member updatedMember = member.toBuilder()
                        .points(rolledBackPoints)
                        .build();
                memberRepository.save(updatedMember);
            }
        }
        pointHistoryRepository.deleteAllById(pointNos);
    }
}