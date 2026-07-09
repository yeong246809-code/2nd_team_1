package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dto.MemberDTO;
import org.example.k_market.entity.Grade;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.GradeRepository;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.UsersRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    // 1. 회원 목록 조회 및 검색
    @Transactional(readOnly = true)
    public Page<MemberDTO> getMembers(String searchType, String keyword, Pageable pageable) {
        Page<Member> memberPage;

        if (keyword == null || keyword.trim().isEmpty()) {
            memberPage = memberRepository.findAll(pageable);
        } else {
            switch (searchType) {
                case "name":
                    memberPage = memberRepository.findByNameContaining(keyword, pageable);
                    break;
                case "email":
                    memberPage = memberRepository.findByEmailContaining(keyword, pageable);
                    break;
                case "phone":
                    memberPage = memberRepository.findByPhoneContaining(keyword, pageable);
                    break;
                case "id":
                    Page<Users> usersPage = usersRepository.findByIdContaining(keyword, pageable);
                    List<Integer> memberNos = usersPage.getContent().stream()
                            .map(Users::getMemberNo)
                            .collect(Collectors.toList());
                    List<Member> matchedMembers = memberRepository.findAllById(memberNos);
                    memberPage = new PageImpl<>(matchedMembers, pageable, usersPage.getTotalElements());
                    break;
                default:
                    memberPage = memberRepository.findAll(pageable);
            }
        }

        List<MemberDTO> dtoList = new ArrayList<>();
        if (memberPage != null && memberPage.hasContent()) {
            List<Integer> ids = memberPage.getContent().stream()
                    .map(Member::getMemberNo).collect(Collectors.toList());

            Map<Integer, String> userIdMap = usersRepository.findAllByMemberNoIn(ids).stream()
                    .collect(Collectors.toMap(Users::getMemberNo, Users::getId));

            dtoList = memberPage.getContent().stream().map(member -> {
                MemberDTO dto = member.toDTO();
                dto.setId(userIdMap.get(member.getMemberNo()));
                return dto;
            }).collect(Collectors.toList());
        }

        return new PageImpl<>(dtoList, pageable, memberPage != null ? memberPage.getTotalElements() : 0);
    }

    // 2. 회원 정보 수정 (모달창 데이터) - toBuilder 활용
    public void updateMember(MemberDTO dto) {
        Member member = memberRepository.findById(dto.getMemberNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // toBuilder()를 사용하여 기존 데이터 유지, 입력받은 값만 변경
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

    // 3. 상태 변경 (정상 <-> 중지/휴면) - toBuilder 활용
    public void changeStatus(int memberNo, String status) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Member updatedMember = member.toBuilder()
                .status(status) // 상태만 덮어쓰기
                .build();

        memberRepository.save(updatedMember);
    }

    // 4. 회원 탈퇴 (비활성)
    @Transactional
    public void deactivateMember(int memberNo) {
        // 1) Users 테이블 상태를 'WITHDRAWN'으로 확실히 처리 (이미 탈퇴 상태라도 덮어쓰기)
        Users user = usersRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 계정입니다."));

        Users updatedUser = user.toBuilder()
                .status("WITHDRAWN")
                .build();

        usersRepository.save(updatedUser);

        // 2) Member 테이블에서 레코드 완전 삭제 (물리적 삭제)
        memberRepository.deleteById(memberNo);
    }

    // 5. 선택 수정 (등급 일괄 변경) - toBuilder 활용
    public void bulkUpdateGrade(List<Integer> memberNos, Map<Integer, Integer> gradeMap) {
        if (memberNos == null || memberNos.isEmpty()) return;

        List<Member> members = memberRepository.findAllById(memberNos);
        List<Member> updatedMembers = new ArrayList<>();

        for (Member member : members) {
            Integer newGrade = gradeMap.get(member.getMemberNo());
            // 등급에 변화가 있는 경우에만 업데이트
            if (newGrade != null && member.getGradeNo() != newGrade) {
                updatedMembers.add(
                        member.toBuilder()
                                .gradeNo(newGrade) // 등급만 덮어쓰기
                                .build()
                );
            }
        }
        memberRepository.saveAll(updatedMembers);
    }
}