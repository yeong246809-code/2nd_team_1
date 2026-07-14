package org.example.k_market.repository;

import org.example.k_market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    List<Product> findByShopNo(Integer shopNo);

    /**
     * 선택한 카테고리(대분류) 및 그 하위 카테고리 번호들에 속한 상품을 조회한다.
     * (대분류를 클릭했을 때 하위 카테고리 상품까지 함께 노출하기 위해 사용)
     *
     * @param cateNos 조회 대상 카테고리 번호 목록 (선택한 카테고리 자신 + 하위 카테고리)
     * @return 해당 카테고리들에 속한 상품 목록
     */
    List<Product> findByCateNoIn(List<Integer> cateNos);

    /**
     * findByCateNoIn과 동일하지만, 상품목록 정렬 옵션
     * (판매많은순 / 낮은가격순 / 높은가격순 / 평점높은순 / 최근등록순)을 함께 적용한다.
     *
     * @param cateNos 조회 대상 카테고리 번호 목록
     * @param sort    정렬 조건
     * @return 정렬된 상품 목록
     */
    List<Product> findByCateNoIn(List<Integer> cateNos, Sort sort);

    /**
     * findByCateNoIn + 정렬에 페이징까지 함께 적용한다.
     * 상품목록 페이지에서 10개씩 페이징 처리할 때 사용.
     *
     * @param cateNos  조회 대상 카테고리 번호 목록
     * @param pageable 정렬 + 페이지 정보 (페이지 번호, 페이지당 개수)
     * @return 페이징된 상품 목록
     */
    Page<Product> findByCateNoIn(List<Integer> cateNos, Pageable pageable);

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
     * 상품명에만 검색어가 포함된 상품을 조회한다. (검색 필터에서 "상품명"만 체크한 경우)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * 상품 설명에만 검색어가 포함된 상품을 조회한다. (검색 필터에서 "설명"만 체크한 경우)
     */
    List<Product> findByDescriptionContainingIgnoreCase(String description);

    /**
     * 상품명 또는 상품 설명에 검색어가 포함된 상품을 조회한다.
     * 영문 검색 시 대소문자를 구분하지 않는다.
     *
     * @param name 상품명 검색어
     * @param description 상품 설명 검색어
     * @return 검색 조건에 맞는 상품 목록
     */
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}