package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.CategoryNodeDTO;
import org.example.k_market.entity.Category;
import org.example.k_market.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 1. 화면에 뿌려줄 카테고리 트리 구조(1차 -> 2차) 만들기
     */
    public List<CategoryNodeDTO> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();
        List<CategoryNodeDTO> tree = new ArrayList<>();

        // ★ 수정: 1차 카테고리를 찾을 때 표준 방식인 'null'만 확인합니다.
        for (Category c : allCategories) {
            if (c.getParentNo() == null) {
                CategoryNodeDTO rootNode = CategoryNodeDTO.builder()
                        .id(c.getCateNo())
                        .name(c.getName())
                        .build();

                for (Category child : allCategories) {
                    if (child.getParentNo() != null && child.getParentNo().equals(c.getCateNo())) {
                        rootNode.getChildren().add(CategoryNodeDTO.builder()
                                .id(child.getCateNo())
                                .name(child.getName())
                                .build());
                    }
                }
                tree.add(rootNode);
            }
        }
        return tree;
    }

    /**
     * 2. 프론트엔드에서 넘어온 전체 구조 일괄 저장 (추가, 수정, 삭제)
     */
    @Transactional
    public void saveAllCategories(List<CategoryNodeDTO> rootNodes) {
        Set<Integer> activeIds = new HashSet<>();

        // 1. 1차 카테고리 (Root) 처리
        for (CategoryNodeDTO rootNode : rootNodes) {
            // ★ 수정: 1차 카테고리를 저장할 때 부모 번호를 'null'로 저장하여 FK 에러 방지
            Category rootCategory = saveOrUpdate(rootNode, null, 1);
            activeIds.add(rootCategory.getCateNo());

            // 2. 2차 카테고리 (Child) 처리
            if (rootNode.getChildren() != null) {
                for (CategoryNodeDTO childNode : rootNode.getChildren()) {
                    Category childCategory = saveOrUpdate(childNode, rootCategory.getCateNo(), 2);
                    activeIds.add(childCategory.getCateNo());
                }
            }
        }

        // 3. 삭제 처리 로직
        List<Category> allInDb = categoryRepository.findAll();
        for (Category dbCategory : allInDb) {
            if (!activeIds.contains(dbCategory.getCateNo())) {
                categoryRepository.delete(dbCategory);
            }
        }
    }

    // 개별 카테고리 추가/수정 공통 로직
    private Category saveOrUpdate(CategoryNodeDTO node, Integer parentNo, int depth) {
        if (node.getId() != null) {
            Category existing = categoryRepository.findById(node.getId()).orElse(null);
            if (existing != null) {
                existing.update(node.getName(), parentNo, depth);
                return existing;
            }
        }
        Category newCategory = Category.builder()
                .name(node.getName())
                .parentNo(parentNo)
                .depth(depth)
                .build();
        return categoryRepository.save(newCategory);
    }
}