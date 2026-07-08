package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Grade;
import org.example.k_market.repository.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultMemberGradeService {

    private static final String DEFAULT_GRADE_NAME = "FAMILY";
    private static final List<StandardGrade> STANDARD_GRADES = List.of(
            new StandardGrade("VVIP", new BigDecimal("5")),
            new StandardGrade("VIP", new BigDecimal("4")),
            new StandardGrade("GOLD", new BigDecimal("3")),
            new StandardGrade("SILVER", new BigDecimal("2")),
            new StandardGrade("FAMILY", new BigDecimal("1"))
    );

    private final GradeRepository gradeRepository;

    @Transactional
    public int getDefaultGradeNo() {
        ensureStandardGrades();
        return gradeRepository.findByName(DEFAULT_GRADE_NAME)
                .map(Grade::getGradeNo)
                .orElseThrow(() -> new IllegalStateException("기본 회원 등급을 찾을 수 없습니다."));
    }

    @Transactional
    public void ensureStandardGrades() {
        for (StandardGrade standardGrade : STANDARD_GRADES) {
            if (gradeRepository.existsByName(standardGrade.name())) {
                gradeRepository.updateRewardRateByName(standardGrade.name(), standardGrade.rewardRate());
            } else {
                gradeRepository.save(Grade.builder()
                        .name(standardGrade.name())
                        .rewardRate(standardGrade.rewardRate())
                        .build());
            }
        }
    }

    private record StandardGrade(String name, BigDecimal rewardRate) {
    }
}
