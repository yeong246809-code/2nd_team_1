package org.example.k_market.config;

import lombok.RequiredArgsConstructor;
import org.example.k_market.service.member.DefaultMemberGradeService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StandardGradeInitializer implements ApplicationRunner {

    private final DefaultMemberGradeService defaultMemberGradeService;

    @Override
    public void run(ApplicationArguments args) {
        defaultMemberGradeService.ensureStandardGrades();
    }
}
