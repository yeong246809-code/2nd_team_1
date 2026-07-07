package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.Qna;
import org.example.k_market.repository.QnaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;


    // 문의 목록 조회
    public List<Qna> findAll() {
        return qnaRepository.findAllByParentNoOrderByNoDesc(0); // 댓글은 출력 안되도록
    }

    // 문의 상세 조회
    public Qna findById(int no) {
        return qnaRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다."));
    }

    // 문의글 등록
    public void save(Qna qna) {
        qna.setCreatedAt(LocalDateTime.now());
        qna.setIsAnswered("검토중");
        qna.setMemberNo(1);      // 임시 로그인 회원
        qna.setParentNo(0);      // 원글
        qna.setViewCount(0);

        qnaRepository.save(qna);
    }

    // 답변 출력
    public Qna findAnswer(int parentNo) {
        return qnaRepository.findByParentNo(parentNo).orElse(null);
    }

    // 문의글 답변
    public void saveAnswer(int parentNo, String content) {

        Qna parent = findById(parentNo);

        Qna answer = Qna.builder()
                .title("답변")
                .content(content)
                .type1(parent.getType1())
                .type2(parent.getType2())
                .memberNo(1)
                .parentNo(parentNo)
                .isAnswered("답변완료")
                .createdAt(LocalDateTime.now())
                .viewCount(0)
                .build();

        qnaRepository.save(answer);

        parent.setIsAnswered("답변완료");
        qnaRepository.save(parent);
    }


}