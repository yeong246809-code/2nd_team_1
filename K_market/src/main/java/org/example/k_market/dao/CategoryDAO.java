package org.example.k_market.dao;

import org.example.k_market.dto.CategoryDTO;
import org.example.k_market.entity.Category;
import org.example.k_market.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class CategoryDAO {

    private final CategoryRepository categoryRepository;

    public CategoryDTO save(CategoryDTO dto) {
        // dto가 null이면 NullPointerException 발생 가능: Service에서 null 체크 후 호출하는 것을 권장
        Category entity = dto.toEntity();
        Category savedEntity = categoryRepository.save(entity);
        return savedEntity.toDTO();
    }

    public Optional<CategoryDTO> findById(Integer catNo) {
        // PK 타입이 엔티티의 @Id 타입과 다르면 컴파일 오류 발생
        return categoryRepository.findById(catNo)
                .map(Category::toDTO);
    }

    public List<CategoryDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                // Entity 내부 연관관계가 LAZY이면 toDTO() 호출 시점에 LazyInitializationException 가능
                .map(Category::toDTO)
                .toList();
    }

    public void deleteById(Integer catNo) {
        // 존재하지 않는 PK 삭제를 막기 위해 existsById로 먼저 확인
        if (categoryRepository.existsById(catNo)) {
            categoryRepository.deleteById(catNo);
        }
    }
}
