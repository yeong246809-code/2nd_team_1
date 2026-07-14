package org.example.k_market.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.repository.CategoryRepository;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.service.ProductService;
import org.example.k_market.service.MainBannerService;
import org.example.k_market.service.admin.SiteConfigService;
import org.example.k_market.service.admin.VersionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
@RequiredArgsConstructor
public class IndexController {

    // 관리자 사이트 설정 정보
    // 사이트명, 로고, 회사 정보, 고객센터 정보 등을 메인 화면에 출력한다.
    private final SiteConfigService siteConfigService;

    // 현재 서비스 버전 정보를 푸터에 출력한다.
    private final VersionService versionService;

    // 메인 화면의 상품 카테고리를 조회한다.
    private final CategoryRepository categoryRepository;

    // 사이드바의 실시간 인기 상품을 조회한다.
    // 기존 팀 작업을 유지하기 위해 제거하지 않는다.
    private final ProductRepository productRepository;

    // 메인 화면 상품 목록과 상품 검색을 처리한다.
    private final ProductService productService;

    private final MainBannerService mainBannerService;


    /**
     * K-market 메인 페이지
     *
     * 사이트 설정, 카테고리, 상품 목록을 조회하여
     * templates/index.html에 전달한다.
     */
    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("topBanner", mainBannerService.findTopBanner().orElse(null));
        model.addAttribute("mainBanners", mainBannerService.findSliderBanners());
        model.addAttribute("leftSideBanner", mainBannerService.findLeftSideBanner().orElse(null));
        model.addAttribute("rightSideBanner", mainBannerService.findRightSideBanner().orElse(null));

        /*
         * 사이트 공통 설정
         * 기존 팀 작업이므로 그대로 유지한다.
         */
        model.addAttribute(
                "siteConfig",
                siteConfigService.getSiteConfig()
        );

        /*
         * 푸터에 출력할 최신 버전 정보
         * 기존 팀 작업이므로 그대로 유지한다.
         */
        model.addAttribute(
                "latestVersion",
                versionService.getLatestVersionCode()
        );

        /*
         * 최상위 카테고리 목록
         * parentNo가 null인 1차 카테고리만 조회한다.
         */
        model.addAttribute(
                "categories",
                categoryRepository.findByParentNoIsNull()
        );

        /*
         * 현재 선택된 메인 카테고리 번호
         * 메인 페이지에서는 선택된 카테고리가 없으므로 null을 전달한다.
         */
        model.addAttribute("mainCateNo", null);

        /*
         * 왼쪽 사이드바 실시간 인기 상품
         * 판매량(salesCount)이 높은 순서대로 3개를 출력한다.
         *
         * 기존 index.html에서 rankingProducts를 사용하고 있으므로 유지한다.
         */
        model.addAttribute(
                "rankingProducts",
                productRepository.findTop3ByOrderBySalesCountDesc()
        );

        /*
         * 베스트 상품
         * 판매량(salesCount)이 높은 순서대로 5개를 출력한다.
         */
        model.addAttribute(
                "bestProducts",
                productService.getBestProducts()
        );

        /*
         * 히트 상품
         * 조회수(viewCount)가 높은 순서대로 8개를 출력한다.
         */
        model.addAttribute(
                "hitProducts",
                productService.getHitProducts()
        );

        /*
         * 추천 상품
         * 추천 상품의 구체적인 기준이 별도로 없으므로
         * 현재는 ProductService에서 정한 기준으로 8개를 조회한다.
         */
        model.addAttribute(
                "recommendedProducts",
                productService.getRecommendedProducts()
        );

        /*
         * 최신 상품
         * 상품 등록일(createdAt)이 최근인 순서대로 8개를 출력한다.
         */
        model.addAttribute(
                "latestProducts",
                productService.getLatestProducts()
        );

        /*
         * 할인 상품
         * 할인율(discountRate)이 높은 순서대로 8개를 출력한다.
         */
        model.addAttribute(
                "discountProducts",
                productService.getDiscountProducts()
        );

        return "index";
    }


    
}
