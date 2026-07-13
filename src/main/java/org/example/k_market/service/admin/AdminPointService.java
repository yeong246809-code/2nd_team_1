package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.AdminPointListDTO;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.PointHistory;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.PointHistoryRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UsersRepository usersRepository;

    // 1. 포인트 목록 조회 및 검색 (PageResponseDTO 공통 페이징 적용)
    @Transactional(readOnly = true)
    public PageResponseDTO<AdminPointListDTO> getPoints(String searchType, String keyword, int pg) {
        // 10개씩 페이징, pointNo 기준 내림차순(최신순) 정렬
        Pageable pageable = PageRequest.of(pg - 1, 10, Sort.by(Sort.Direction.DESC, "pointNo"));
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
                pointPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
            } else {
                // 찾은 회원 번호들로 포인트 내역 조회
                pointPage = pointHistoryRepository.findByMemberNoIn(memberNos, pageable);
            }
        }

        // Entity -> DTO 변환
        List<AdminPointListDTO> dtoList = pointPage.getContent().stream().map(history -> {
            Member member = memberRepository.findById(history.getMemberNo()).orElse(null);
            Users user = usersRepository.findByMemberNo(history.getMemberNo()).orElse(null);

            return AdminPointListDTO.builder()
                    .pointNo(history.getPointNo())
                    .id(user != null ? user.getId() : "탈퇴회원")
                    .name(member != null ? member.getName() : "알수없음")
                    .amount(history.getAmount())
                    // ★ 의도하신 대로: 화면의 '잔여 포인트(remainedAmount)' 열에 회원의 실제 현재 보유 포인트(member.getPoints()) 매핑!
                    .remainedAmount(member != null ? member.getPoints() : history.getRemainedAmount())
                    .description(history.getDescription())
                    .createdAt(history.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        Page<AdminPointListDTO> dtoPage = new PageImpl<>(dtoList, pageable, pointPage.getTotalElements());
        return new PageResponseDTO<>(dtoPage, 5); // 블록 사이즈 5
    }

    // 2. 선택된 포인트 내역 삭제 및 회원 포인트 회수 처리
    public void deletePointHistories(List<Long> pointNos) {
        if (pointNos == null || pointNos.isEmpty()) return;

        List<PointHistory> histories = pointHistoryRepository.findAllById(pointNos);

        for (PointHistory history : histories) {
            Member member = memberRepository.findById(history.getMemberNo()).orElse(null);
            if (member != null) {
                // ★ 정상 복구: 삭제되는 내역의 지급/사용 금액(amount)만큼 회원 보유 포인트에서 차감/회수
                int rolledBackPoints = Math.max(0, member.getPoints() - history.getAmount());
                Member updatedMember = member.toBuilder()
                        .points(rolledBackPoints)
                        .build();
                memberRepository.save(updatedMember);
            }
        }
        pointHistoryRepository.deleteAllById(pointNos);
    }
}