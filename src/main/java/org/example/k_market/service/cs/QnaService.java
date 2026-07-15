package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
import org.example.k_market.entity.Notice;
import org.example.k_market.entity.Qna;
import org.example.k_market.repository.QnaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;

    // 사용자 문의 목록 페이지 조회
    // 화면의 pg는 1부터 시작하지만 PageRequest는 0부터 시작한다.
    @Transactional(readOnly = true)
    public Page<Qna> findAll(int pg, int size) {

        int pageIndex = Math.max(pg - 1, 0);

        PageRequest pageable = PageRequest.of(
                pageIndex,
                size,
                Sort.by(Sort.Direction.DESC, "no")
        );

        return qnaRepository.findAllByParentNo(0, pageable);
    }

    // 페이지네이션이 필요 없는 기존 호출을 위한 메서드
    @Transactional(readOnly = true)
    public List<Qna> findAll() {
        return qnaRepository.findAllByParentNoOrderByNoDesc(0);
    }

    @Transactional(readOnly = true)
    public List<Qna> findTop5() {
        return qnaRepository.findTop5ByParentNoOrderByNoDesc(0);
    }

    @Transactional(readOnly = true)
    public Qna findById(int no) {
        return qnaRepository.findById(no)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 문의글입니다."));
    }

    @Transactional
    public void save(QnaDTO dto) {

        Qna question = Qna.builder()
                // 원글도 신규 행이므로 no를 넣지 않는다.
                .type1(dto.getType1())
                .type2(dto.getType2())
                .title(dto.getTitle())
                .content(dto.getContent())
                .memberNo(dto.getMemberNo())
                .prodNo(dto.getProdNo())

                // 원글과 답변을 구분하는 핵심 값
                .parentNo(0)
                .isAnswered("답변대기")

                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        qnaRepository.save(question);
    }

    @Transactional(readOnly = true)
    public Qna findAnswer(int parentNo) {
        return qnaRepository.findByParentNo(parentNo).orElse(null);
    }

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

    @Transactional
    public void saveOrUpdateAnswer(int parentNo, String content) {
        saveAnswer(parentNo, content);
    }

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

    @Transactional
    public void deleteChecked(List<Integer> nos) {

        for (Integer no : nos) {
            qnaRepository.deleteByParentNo(no);
        }

        qnaRepository.deleteAllById(nos);
    }

    @Transactional
    public Qna getQnaAndIncreaseViewCount(int no) {
        Qna qna = qnaRepository.findById(no)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문의글입니다."));

        // 조회수 1 증가 (Qna 엔티티에 setViewCount 메서드가 있어야 합니다)
        qna.setViewCount(qna.getViewCount() + 1);

        return qna; // @Transactional이 붙어있으므로 메서드 종료 시 DB에 자동으로 UPDATE 됩니다.
    }


}
