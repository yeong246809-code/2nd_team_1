package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
import org.example.k_market.entity.Qna;
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
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 문의글입니다."));
    }

    // QnA 글 등록
    // prodNo는 QnaDTO.toEntity()에서 Entity로 전달되어야 한다.
    public void save(QnaDTO dto) {
        qnaRepository.save(dto.toEntity());
    }

    // 답변 출력
    // parentNo가 현재 문의글 번호인 답변글을 조회한다.
    public Qna findAnswer(int parentNo) {
        return qnaRepository.findByParentNo(parentNo).orElse(null);
    }

    // 문의글 답변 등록 및 수정
    @Transactional
    public void saveAnswer(int parentNo, String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        Qna parent = findById(parentNo);
        Qna answer = findAnswer(parentNo);

        if (answer == null) {
            answer = Qna.builder()
                    .title("답변")
                    .content(content)
                    .type1(parent.getType1())
                    .type2(parent.getType2())
                    .memberNo(1)
                    .prodNo(parent.getProdNo())
                    .parentNo(parentNo)
                    .isAnswered("답변완료")
                    .createdAt(LocalDateTime.now())
                    .viewCount(0)
                    .build();
        } else {
            answer.setContent(content);
            answer.setCreatedAt(LocalDateTime.now());
        }

        qnaRepository.save(answer);

        parent.setIsAnswered("답변완료");
        qnaRepository.save(parent);
    }

    // 답변 등록 또는 수정
    @Transactional
    public void saveOrUpdateAnswer(int parentNo, String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        Qna parent = findById(parentNo);
        Qna answer = findAnswer(parentNo);

        if (answer == null) {
            answer = Qna.builder()
                    .title("답변")
                    .content(content)
                    .type1(parent.getType1())
                    .type2(parent.getType2())
                    .memberNo(1)
                    .prodNo(parent.getProdNo())
                    .parentNo(parentNo)
                    .isAnswered("답변완료")
                    .createdAt(LocalDateTime.now())
                    .viewCount(0)
                    .build();
        } else {
            answer.setContent(content);
            answer.setCreatedAt(LocalDateTime.now());
        }

        qnaRepository.save(answer);

        parent.setIsAnswered("답변완료");
        qnaRepository.save(parent);
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(int parentNo) {

        Qna parent = findById(parentNo);
        Qna answer = findAnswer(parentNo);

        if (answer != null) {
            qnaRepository.delete(answer);
        }

        parent.setIsAnswered("답변대기");
        qnaRepository.save(parent);
    }

    // 선택 문의 삭제
    @Transactional
    public void deleteChecked(List<Integer> nos) {
        qnaRepository.deleteAllById(nos);
    }
}