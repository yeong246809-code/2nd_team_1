package org.example.k_market.repository.custom;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.k_market.entity.QShop;
import org.example.k_market.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.example.k_market.entity.QShop.shop; // static import로 간결하게 사용

@RequiredArgsConstructor
public class ShopRepositoryCustomImpl implements ShopRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Shop> searchShops(String searchType, String keyword, String statusFilter, String sort, Pageable pageable) {

        // 1. 실제 데이터 목록 조회 쿼리 (아주 간결하고 직관적!)
        List<Shop> content = queryFactory
                .selectFrom(shop)
                .where(
                        shop.status.ne("DELETED").or(shop.status.isNull()), // 삭제되지 않은 상점
                        statusEq(statusFilter),                             // 동적 상태 필터
                        keywordContains(searchType, keyword)                // 동적 검색어 필터
                )
                .orderBy(getOrderSpecifier(sort))                           // 동적 정렬
                .offset(pageable.getOffset())                               // 페이징 시작 위치
                .limit(pageable.getPageSize())                              // 페이지당 개수
                .fetch();

        // 2. 전체 데이터 개수 카운트 쿼리 (페이징 계산용)
        JPAQuery<Long> countQuery = queryFactory
                .select(shop.count())
                .from(shop)
                .where(
                        shop.status.ne("DELETED").or(shop.status.isNull()),
                        statusEq(statusFilter),
                        keywordContains(searchType, keyword)
                );

        // 3. Spring Data JPA의 Page 객체로 변환하여 반환
        return new PageImpl<>(content, pageable, countQuery.fetchOne());
    }

    // ================= [동적 조건 조립 메서드들] ================= //

    // ① 상태 필터 동적 조립 (null 반환 시 where 절에서 자동 무시됨!)
    private BooleanExpression statusEq(String statusFilter) {
        if (!StringUtils.hasText(statusFilter)) {
            return null; // "전체보기"일 때는 조건 자체를 걸지 않음!
        }
        if ("PENDING".equals(statusFilter)) {
            return shop.status.eq("운영준비").or(shop.status.eq("PENDING"));
        } else if ("ACTIVE".equals(statusFilter)) {
            return shop.status.eq("운영중").or(shop.status.eq("ACTIVE"));
        } else if ("STOPPED".equals(statusFilter)) {
            return shop.status.eq("운영중지").or(shop.status.eq("STOPPED"));
        }
        return shop.status.eq(statusFilter);
    }

    // ② 검색어 필터 동적 조립
    private BooleanExpression keywordContains(String searchType, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null; // 검색어가 없으면 where 절에서 자동 무시!
        }
        switch (searchType) {
            case "name":      return shop.name.contains(keyword); // LIKE '%keyword%'와 동일
            case "ceo":       return shop.ceo.contains(keyword);
            case "bizNumber": return shop.bizNumber.contains(keyword);
            case "phone":     return shop.phone.contains(keyword);
            default:          return shop.name.contains(keyword).or(shop.ceo.contains(keyword));
        }
    }

    // ③ 동적 정렬 조립 (운영준비 최상단 커스텀 정렬도 깔끔하게 해결!)
    private OrderSpecifier<?>[] getOrderSpecifier(String sort) {
        if ("idAsc".equals(sort)) {
            return new OrderSpecifier[]{ shop.shopNo.coalesce(shop.memberNo).asc() };
        } else if ("idDesc".equals(sort)) {
            return new OrderSpecifier[]{ shop.shopNo.coalesce(shop.memberNo).desc() };
        } else {
            // 기본 정렬: '운영준비' 상태인 경우 0, 아니면 1로 평가하여 오름차순 정렬 후 -> 번호 내림차순
            OrderSpecifier<Integer> pendingPriority = new CaseBuilder()
                    .when(shop.status.eq("운영준비").or(shop.status.eq("PENDING"))).then(0)
                    .otherwise(1)
                    .asc();
            return new OrderSpecifier[]{ pendingPriority, shop.shopNo.coalesce(shop.memberNo).desc() };
        }
    }
}