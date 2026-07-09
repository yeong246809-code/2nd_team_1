package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
import org.example.k_market.entity.Qna;
import org.example.k_market.repository.NoticeRepository;
import org.example.k_market.repository.QnaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;

    // 문의 목록 조회
    // parentNo가 0인 원글만 조회해서 답변글은 목록에 출력되지 않도록 한다.
    public List<Qna> findAll() {
        return qnaRepository.findAllByParentNoOrderByNoDesc(0);
    }

    // 고객센터 메인 문의글 최신 5개 조회
    // parentNo가 0인 원글만 가져온다.
    public List<Qna> findTop5() {
        return qnaRepository.findTop5ByParentNoOrderByNoDesc(0);
    }

    // 문의 상세 조회
    public Qna findById(int no) {
        return qnaRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다."));
    }

    // QnA 글 등록
    // Controller에서 받은 DTO를 Entity로 변환한 뒤 Repository로 저장한다.
    public void save(QnaDTO qnaDTO) {

        Qna qna = Qna.builder()
                .type1(qnaDTO.getType1())
                .type2(qnaDTO.getType2())
                .title(qnaDTO.getTitle())
                .content(qnaDTO.getContent())
                .memberNo(qnaDTO.getMemberNo())
                .parentNo(0)
                .isAnswered("답변대기")
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        qnaRepository.save(qna);
    }

    // 답변 출력
    // parentNo가 현재 문의글 번호인 답변글을 조회한다.
    public Qna findAnswer(int parentNo) {
        return qnaRepository.findByParentNo(parentNo).orElse(null);
    }

    // 문의글 답변 등록
    public void saveAnswer(int parentNo, String content) {

        // 원본 문의글 조회
        Qna parent = findById(parentNo);

        // 답변글 생성
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

        // 답변글 저장
        qnaRepository.save(answer);

        // 원본 문의글 상태 변경
        parent.setIsAnswered("답변완료");
        qnaRepository.save(parent);


    }

    public void saveOrUpdateAnswer(int parentNo, String content) {

        Qna parent = findById(parentNo);
        Qna answer = findAnswer(parentNo);

        if (answer == null) {
            answer = Qna.builder()
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
        } else {
            answer.setContent(content);
        }

        qnaRepository.save(answer);

        parent.setIsAnswered("답변완료");
        qnaRepository.save(parent);
    }


    public void deleteAnswer(int parentNo) {

        Qna parent = findById(parentNo);
        Qna answer = findAnswer(parentNo);

        if (answer != null) {
            qnaRepository.delete(answer);
        }

        parent.setIsAnswered("답변대기");
        qnaRepository.save(parent);
    }

    @Transactional
    public void deleteChecked(List<Integer> nos) {
        qnaRepository.deleteAllById(nos);
    }

}