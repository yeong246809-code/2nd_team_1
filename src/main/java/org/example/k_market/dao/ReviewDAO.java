package org.example.k_market.dao;

import org.example.k_market.dto.ReviewDTO;
import org.example.k_market.entity.Review;
import org.example.k_market.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ReviewDAO {

    private final ReviewRepository reviewRepository;

    public ReviewDTO save(ReviewDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Review entity = dto.toEntity();
        Review savedEntity = reviewRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<ReviewDTO> findById(Long reviewNO) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return reviewRepository.findById(reviewNO)
                .map(Review::toDTO);
    }

    public List<ReviewDTO> findAll() {
        return reviewRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Review::toDTO)
                .toList();
    }

    public void deleteById(Long reviewNO) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (reviewRepository.existsById(reviewNO)) {
            reviewRepository.deleteById(reviewNO);
        }
    }
}
