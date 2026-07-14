package org.example.k_market.controller.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.entity.Cart;
import org.example.k_market.entity.Category;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.Qna;
import org.example.k_market.entity.Review;
import org.example.k_market.repository.CartRepository;
import org.example.k_market.repository.CategoryRepository;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.repository.QnaRepository;
import org.example.k_market.repository.ReviewRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor // Repository 자동 주입을 위해 필수!
public class ProductIndexController {

    private final CategoryRepository categoryRepository; // 이미 있을 가능성 높음
    private final ProductRepository productRepository;   // 탭에 열려있던 그거
    private final CartRepository cartRepository;         // 신규 주입
    private final ReviewRepository reviewRepository;     // 신규 주입
    private final QnaRepository qnaRepository;           // 신규 주입

    private static final int PAGE_SIZE = 10;

    @GetMapping("/product/list")
    public String list(@RequestParam(required = false) Integer cateNo,
                       @RequestParam(required = false, defaultValue = "latest") String sort,
                       @RequestParam(required = false, defaultValue = "1") int page,
                       Model model) {

        int pageIndex = Math.max(page - 1, 0); // 화면은 1부터, Pageable은 0부터
        List<Product> products;
        int totalPages;

        if ("reviewCount".equals(sort)) {
            // 후기많은순은 DB 정렬이 아니라 리뷰 개수 기준 자바 정렬이라 직접 페이징 처리
            List<Product> all = (cateNo == null)
                    ? productRepository.findAll()
                    : productRepository.findByCateNoIn(resolveCateNos(cateNo));
            all = sortByReviewCountDesc(all);

            totalPages = (int) Math.ceil((double) all.size() / PAGE_SIZE);
            int fromIndex = Math.min(pageIndex * PAGE_SIZE, all.size());
            int toIndex = Math.min(fromIndex + PAGE_SIZE, all.size());
            products = all.subList(fromIndex, toIndex);
        } else {
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(pageIndex, PAGE_SIZE, resolveSort(sort));

            org.springframework.data.domain.Page<Product> productPage = (cateNo == null)
                    ? productRepository.findAll(pageable)
                    : productRepository.findByCateNoIn(resolveCateNos(cateNo), pageable);

            products = productPage.getContent();
            totalPages = productPage.getTotalPages();
        }

        addProductLayout(model, cateNo);
        model.addAttribute("products", products);
        model.addAttribute("sort", sort);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", Math.max(totalPages, 1));
        return "product/list";
    }

    /**
     * 상품정렬 스펙:
     * 판매많은순 / 낮은가격순 / 높은가격순 / 평점높은순 / 후기많은순 / 최근등록순
     */
    private org.springframework.data.domain.Sort resolveSort(String sort) {
        return switch (sort) {
            case "salesDesc" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "salesCount");
            case "priceAsc" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.ASC, "price");
            case "priceDesc" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "price");
            case "ratingDesc" -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "rating");
            case "reviewCount" -> org.springframework.data.domain.Sort.unsorted(); // 아래에서 별도 처리
            default -> org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "createdAt"); // 최근등록순 (기본값)
        };
    }

    /**
     * 후기많은순: 상품별 리뷰 개수를 기준으로 내림차순 정렬
     */
    private List<Product> sortByReviewCountDesc(List<Product> products) {
        return products.stream()
                .sorted((a, b) -> Long.compare(
                        reviewRepository.countByProdNo(b.getProdNo()),
                        reviewRepository.countByProdNo(a.getProdNo())
                ))
                .toList();
    }

    /**
     * 선택한 카테고리(cateNo)가 상위(대분류)인 경우, 그 하위 카테고리 번호까지
     * 모두 포함해서 조회 대상으로 만들어준다.
     * (상품의 cateNo는 실제로는 하위 카테고리 번호를 참조하는 경우가 많기 때문)
     */
    private List<Integer> resolveCateNos(Integer cateNo) {
        List<Integer> cateNos = new java.util.ArrayList<>();
        cateNos.add(cateNo);

        List<Category> children = categoryRepository.findByParentNo(cateNo);
        for (Category child : children) {
            cateNos.add(child.getCateNo());
        }

        return cateNos;
    }

    @GetMapping("/product/search")
    public String search(@RequestParam(value = "keyword", defaultValue = "셔츠") String keyword, Model model) {
        List<Map<String, Object>> products = List.of(
                Map.of(
                        "name", "이지 워시 옥스퍼드 셔츠",
                        "description", "데일리 아이템으로 입기 좋은 스탠다드 핏 셔츠입니다.",
                        "isNew", true,
                        "isFreeShipping", true,
                        "discount", 10,
                        "originalPrice", "30,000",
                        "price", "27,000",
                        "seller", "패션빌리지"
                )
        );

        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("products", products);
        addProductLayout(model, null);

        return "product/search";
    }

    @GetMapping("/product/view")
    public String view(@RequestParam Integer prodNo, Model model) {
        Product product = productRepository.findById(Long.valueOf(prodNo))
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + prodNo));

        // 조회수 +1 (상세페이지 진입 시마다 증가)
        int currentViewCount = (product.getViewCount() == null) ? 0 : product.getViewCount();
        product.setViewCount(currentViewCount + 1);
        productRepository.save(product);

        Category category = categoryRepository.findById(product.getCateNo()).orElse(null);
        Category parentCategory = (category != null && category.getParentNo() != null)
                ? categoryRepository.findById(category.getParentNo()).orElse(null)
                : null;

        // 사이드바 활성화 표시용 - 대분류 catNo
        Integer mainCateNo = (parentCategory != null) ? parentCategory.getCateNo() : (category != null ? category.getCateNo() : null);

        // 상품후기 목록 (최신순)
        List<Review> reviewList = reviewRepository.findByProdNoOrderByCreatedAtDesc(product.getProdNo());

        // 상품 Q&A 목록 (해당 상품에 달린 문의 원글만, parentNo=0)
        List<Qna> qnaList = qnaRepository.findByProdNoAndParentNoOrderByNoDesc(product.getProdNo(), 0);

        model.addAttribute("product", product);
        model.addAttribute("parentCategory", parentCategory);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("qnaList", qnaList);
        addProductLayout(model, mainCateNo);
        return "product/view";
    }

    /**
     * 장바구니 담기
     * - 로그인 안 되어 있으면 로그인 페이지로 보냄
     * - 같은 상품이 이미 담겨 있어도 우선 새 행으로 추가 (중복 병합 로직은 다음 단계에서 개선)
     */
    @PostMapping("/product/cart/add")
    public String addToCart(@RequestParam Long prodNo,
                            @RequestParam(defaultValue = "1") int quantity,
                            @AuthenticationPrincipal MyUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        Cart cart = Cart.builder()
                .memberNo(userDetails.getUser().getMemberNo())
                .prodNo(prodNo)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .build();
        cartRepository.save(cart);

        return "redirect:/product/cart";
    }

    /**
     * 바로구매 - 결제 페이지(주문서)로 이동
     * (실제 결제/주문 저장 로직은 별도 OrderController에서 추가 구현 필요)
     */
    @PostMapping("/product/order/direct")
    public String orderDirect(@RequestParam Long prodNo,
                              @RequestParam(defaultValue = "1") int quantity,
                              Model model,
                              @AuthenticationPrincipal MyUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        Product product = productRepository.findById(prodNo)
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + prodNo));

        model.addAttribute("product", product);
        model.addAttribute("quantity", quantity);
        addProductLayout(model, product.getCateNo());
        return "product/order";
    }

    @GetMapping("/product/cart")
    public String cart(Model model) {
        addProductLayout(model, null);
        return "product/cart";
    }

    @GetMapping("/product/order")
    public String order(Model model) {
        model.addAttribute("product", sampleProduct());
        model.addAttribute("order", Map.of("totalPrice", "24,650"));
        addProductLayout(model, null);
        return "product/order";
    }

    /**
     * 상품후기 등록
     */
    @PostMapping("/product/review/add")
    public String addReview(@RequestParam Long prodNo,
                            @RequestParam int rating,
                            @RequestParam String content,
                            @AuthenticationPrincipal MyUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        Review review = Review.builder()
                .prodNo(prodNo)
                .memberNo(userDetails.getUser().getMemberNo())
                .rating(rating)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        reviewRepository.save(review);

        return "redirect:/product/view?prodNo=" + prodNo;
    }

    private Map<String, Object> sampleProduct() {
        return Map.ofEntries(
                Map.entry("id", 1001),
                Map.entry("name", "프리미엄 데일리 상품"),
                Map.entry("category", "패션"),
                Map.entry("subcategory", "셔츠"),
                Map.entry("price", "27,000"),
                Map.entry("originalPrice", "30,000"),
                Map.entry("origin", "대한민국"),
                Map.entry("sellerName", "K-market"),
                Map.entry("modelName", "KM-SAMPLE-001"),
                Map.entry("description", "샘플 상품 설명")
        );
    }

    private void addProductLayout(Model model, Integer mainCateNo) {
        model.addAttribute("categories", categoryRepository.findByParentNoIsNull());
        model.addAttribute("mainCateNo", mainCateNo);
        model.addAttribute("rankingProducts", productRepository.findTop3ByOrderBySalesCountDesc());

        String mainCateName = (mainCateNo == null)
                ? null
                : categoryRepository.findById(mainCateNo)
                .map(Category::getName)
                .orElse(null);
        model.addAttribute("mainCateName", mainCateName);
    }
}