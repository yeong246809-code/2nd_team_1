package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public String createCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("[K-market] 회원가입 이메일 인증번호");
        message.setText("""
                K-market 회원가입 이메일 인증번호입니다.

                인증번호: %s

                인증번호는 5분 동안 사용할 수 있습니다.
                """.formatted(code));

        mailSender.send(message);
    }
}
