package org.example.k_market.service.cs;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
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

    public void save(QnaDTO dto) {
        qnaRepository.save(dto.toEntity());
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
        qnaRepository.deleteAllById(nos);
    }
}
