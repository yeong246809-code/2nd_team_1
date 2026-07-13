package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.MemberDTO;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.member.MemberAccountStatus;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminMemberService {

    private final MemberRepository memberRepository;
    private final UsersRepository usersRepository;

    // 1. 회원 목록 조회 (페이징 + 정렬 + 상태 필터 + 검색)
    @Transactional(readOnly = true)
    public PageResponseDTO<MemberDTO> getMembers(String searchType, String keyword, String statusFilter, String sort, int pg) {
        // 1) 정렬 조건 설정 (기본값: 번호 내림차순, 페이지당 10개)
        Sort.Direction direction = "idAsc".equals(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(pg - 1, 5, Sort.by(direction, "memberNo"));

        // 2) 상태 필터 변환
        MemberAccountStatus statusObj = null;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            try {
                statusObj = MemberAccountStatus.valueOf(statusFilter);
            } catch (Exception e) {
                log.warn("Invalid status filter: {}", statusFilter);
            }
        }

        // 3) 검색어 및 검색조건에 따른 쿼리 분기
        Page<Member> memberPage;
        if (keyword == null || keyword.trim().isEmpty()) {
            memberPage = memberRepository.findAllByStatus(statusObj, pageable);
        } else {
            switch (searchType) {
                case "name":
                    memberPage = memberRepository.findByNameAndStatus(keyword, statusObj, pageable);
                    break;
                case "email":
                    memberPage = memberRepository.findByEmailAndStatus(keyword, statusObj, pageable);
                    break;
                case "phone":
                    memberPage = memberRepository.findByPhoneAndStatus(keyword, statusObj, pageable);
                    break;
                case "id":
                    Page<Users> usersPage = usersRepository.findByIdContaining(keyword, Pageable.unpaged());
                    List<Integer> memberNos = usersPage.getContent().stream()
                            .map(Users::getMemberNo)
                            .collect(Collectors.toList());
                    memberPage = memberNos.isEmpty() ? new PageImpl<>(new ArrayList<>(), pageable, 0)
                            : memberRepository.findByMemberNosAndStatus(memberNos, statusObj, pageable);
                    break;
                default:
                    memberPage = memberRepository.findAllByStatus(statusObj, pageable);
            }
        }

        // 4) Member -> MemberDTO 변환
        List<MemberDTO> dtoList = new ArrayList<>();
        if (memberPage != null && memberPage.hasContent()) {
            List<Integer> ids = memberPage.getContent().stream().map(Member::getMemberNo).collect(Collectors.toList());
            Map<Integer, String> userIdMap = usersRepository.findAllByMemberNoIn(ids).stream()
                    .collect(Collectors.toMap(Users::getMemberNo, Users::getId, (oldVal, newVal) -> oldVal));

            dtoList = memberPage.getContent().stream().map(member -> {
                MemberDTO dto = member.toDTO();
                dto.setId(userIdMap.get(member.getMemberNo()));
                return dto;
            }).collect(Collectors.toList());
        }

        // 5) PageResponseDTO 반환
        Page<MemberDTO> dtoPage = new PageImpl<>(dtoList, pageable, memberPage != null ? memberPage.getTotalElements() : 0);
        return new PageResponseDTO<>(dtoPage, 5);
    }

    // 2. 회원 정보 수정
    public void updateMember(MemberDTO dto) {
        Member member = memberRepository.findById(dto.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Member updatedMember = member.toBuilder()
                .name(dto.getName())
                .gender(dto.getGender())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .zipCode(dto.getZipCode())
                .baseAddress(dto.getBaseAddress())
                .detailAddress(dto.getDetailAddress())
                .memo(dto.getMemo())
                .build();
        memberRepository.save(updatedMember);
    }

    // 3. 상태 변경
    public void changeStatus(int memberNo, String status) {
        MemberAccountStatus accountStatus = MemberAccountStatus.valueOf(status);
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Member updatedMember = member.toBuilder().status(accountStatus).build();
        Users user = usersRepository.findByMemberNo(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 계정입니다."));
        Users updatedUser = user.toBuilder().status(accountStatus).build();

        memberRepository.save(updatedMember);
        usersRepository.save(updatedUser);
    }

    // 4. 회원 탈퇴
    @Transactional
    public void deactivateMember(int memberNo) {
        Users user = usersRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 계정입니다."));
        usersRepository.save(user.toBuilder().status(MemberAccountStatus.WITHDRAWN).build());
        memberRepository.deleteById(memberNo);
    }

    // 5. 선택 수정 (등급 일괄 변경)
    public void bulkUpdateGrade(List<Integer> memberNos, Map<Integer, Integer> gradeMap) {
        if (memberNos == null || memberNos.isEmpty()) return;
        List<Member> members = memberRepository.findAllById(memberNos);
        List<Member> updatedMembers = members.stream().map(m -> {
            Integer newGrade = gradeMap.get(m.getMemberNo());
            return (newGrade != null && !newGrade.equals(m.getGradeNo())) ? m.toBuilder().gradeNo(newGrade).build() : m;
        }).collect(Collectors.toList());
        memberRepository.saveAll(updatedMembers);
    }
}