package org.example.k_market.repository;

import org.example.k_market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Product 엔티티의 데이터 조회와 저장을 담당하는 Repository.
 *
 * Spring Data JPA 메서드 이름 규칙을 이용하여
 * 별도의 JPQL 없이 카테고리, 검색, 정렬 조건을 처리한다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 선택한 카테고리에 속한 상품을 조회한다.
     *
     * @param cateNo 카테고리 번호
     * @return 해당 카테고리의 상품 목록
     */
    List<Product> findByCateNo(Integer cateNo);

    List<Product> findByCateNoIn(List<Integer> cateNos);

    List<Product> findByCateNoIn(List<Integer> cateNos, Sort sort);

    Page<Product> findByCateNoIn(List<Integer> cateNos, Pageable pageable);

    List<Product> findTop3ByOrderBySalesCountDesc();
    /**
     * 메인 사이드바용 인기 상품을 조회한다.
     * 판매량이 높은 순서대로 최대 3개를 반환한다.
     */
    List<Product> findTop3ByOrderBySalesCountDesc();

    /**
     * 메인 베스트 상품을 조회한다.
     * 판매량이 높은 순서대로 최대 5개를 반환한다.
     */
    List<Product> findTop5ByOrderBySalesCountDesc();

    /**
     * 메인 히트 상품을 조회한다.
     * 조회수가 높은 순서대로 최대 8개를 반환한다.
     */
    List<Product> findTop8ByOrderByViewCountDesc();

    /**
     * 메인 추천 상품을 조회한다.
     * 평점이 높은 순서대로 최대 8개를 반환한다.
     */
    List<Product> findTop8ByOrderByRatingDesc();

    /**
     * 메인 최신 상품을 조회한다.
     * 등록일이 최근인 순서대로 최대 8개를 반환한다.
     */
    List<Product> findTop8ByOrderByCreatedAtDesc();

    /**
     * 메인 할인 상품을 조회한다.
     * 할인율이 높은 순서대로 최대 8개를 반환한다.
     */
    List<Product> findTop8ByOrderByDiscountRateDesc();

    /**
     * 상품명 또는 상품 설명에 검색어가 포함된 상품을 조회한다.
     * 영문 검색 시 대소문자를 구분하지 않는다.
     *
     * @param name 상품명 검색어
     * @param description 상품 설명 검색어
     * @return 검색 조건에 맞는 상품 목록
     */
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description
    );
}