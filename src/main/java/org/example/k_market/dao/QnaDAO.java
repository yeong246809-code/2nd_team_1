package org.example.k_market.dao;

import org.example.k_market.dto.QnaDTO;
import org.example.k_market.entity.Qna;
import org.example.k_market.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class QnaDAO {

    private final QnaRepository qnaRepository;

    public QnaDTO save(QnaDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Qna entity = dto.toEntity();
        Qna savedEntity = qnaRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<QnaDTO> findById(Integer no) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return qnaRepository.findById(no)
                .map(Qna::toDTO);
    }

    public List<QnaDTO> findAll() {
        return qnaRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Qna::toDTO)
                .toList();
    }

    public void deleteById(Integer no) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (qnaRepository.existsById(no)) {
            qnaRepository.deleteById(no);
        }
    }
}
