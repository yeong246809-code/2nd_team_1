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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor // Repository 자동 주입을 위해 필수!
@RequestMapping("/product")
public class ProductIndexController {

    private final CategoryRepository categoryRepository; // 이미 있을 가능성 높음
    private final ProductRepository productRepository;   // 탭에 열려있던 그거
    private final CartRepository cartRepository;         // 신규 주입
    private final ReviewRepository reviewRepository;     // 신규 주입
    private final QnaRepository qnaRepository;           // 신규 주입

    private static final int PAGE_SIZE = 10;

    /**
     * 상품 목록을 조회한다.
     *
     * cateNo가 전달되면 해당 2차 카테고리 상품만 조회한다.
     * parentCateNo가 전달되면 해당 1차 카테고리와 연결된
     * 모든 2차 카테고리 상품을 조회한다.
     */
    @GetMapping("/list")
    public String list(
            @RequestParam(required = false) Integer cateNo,
            @RequestParam(required = false) Integer parentCateNo,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {

        List<Product> products;
        Integer activeCateNo = null;
        String mainCateName = "전체";

        /*
         * 2차 카테고리 선택
         * 예: 아우터(cateNo=2), 상의(cateNo=3)
         */
        if (cateNo != null) {
            products = new ArrayList<>(
                    productRepository.findByCateNo(cateNo)
            );

            activeCateNo = cateNo;

            Category selectedCategory =
                    categoryRepository.findById(cateNo).orElse(null);

            if (selectedCategory != null) {
                mainCateName = selectedCategory.getName();
            }

            /*
             * 1차 카테고리 선택
             * 예: 패션(parentCateNo=1), 뷰티(parentCateNo=7)
             */
        } else if (parentCateNo != null) {

            List<Integer> childCateNos =
                    categoryRepository
                            .findByParentNoOrderByCateNoAsc(parentCateNo)
                            .stream()
                            .map(Category::getCateNo)
                            .toList();

            /*
             * 하위 카테고리가 있으면 하위 카테고리 상품 전체 조회,
             * 없으면 1차 카테고리 번호에 직접 연결된 상품 조회
             */
            if (childCateNos.isEmpty()) {
                products = new ArrayList<>(
                        productRepository.findByCateNo(parentCateNo)
                );
            } else {
                products = new ArrayList<>(
                        productRepository.findByCateNoIn(childCateNos)
                );
            }

            activeCateNo = parentCateNo;

            Category selectedCategory =
                    categoryRepository.findById(parentCateNo).orElse(null);

            if (selectedCategory != null) {
                mainCateName = selectedCategory.getName();
            }

            /*
             * 카테고리 조건이 없으면 전체 상품 조회
             */
        } else {
            products = new ArrayList<>(productRepository.findAll());
        }

        /*
         * 선택한 정렬 조건 적용
         */
        String normalizedSort =
                (sort == null || sort.isBlank()) ? "latest" : sort;

        sortProducts(products, normalizedSort);

        /*
         * 조회된 목록을 10개씩 나누어 현재 페이지에 출력
         */
        int totalCount = products.size();
        int totalPages = (int) Math.ceil(
                (double) totalCount / PAGE_SIZE
        );

        int currentPage = Math.max(page, 1);

        if (totalPages > 0 && currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = totalPages == 0
                ? 0
                : (currentPage - 1) * PAGE_SIZE;

        int toIndex = Math.min(
                fromIndex + PAGE_SIZE,
                totalCount
        );

        List<Product> pageProducts =
                new ArrayList<>(products.subList(fromIndex, toIndex));

        /*
         * 상품 페이지 공통 레이아웃 데이터
         */
        addProductLayout(model, activeCateNo);

        /*
         * 목록 화면에서 사용할 데이터
         */
        model.addAttribute("products", pageProducts);
        model.addAttribute("sort", normalizedSort);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("mainCateName", mainCateName);

        /*
         * 정렬 및 페이지 이동 시 현재 카테고리 조건을 유지하기 위한 값
         */
        model.addAttribute("selectedCateNo", cateNo);
        model.addAttribute("selectedParentCateNo", parentCateNo);

        return "product/list";
    }

    /**
     * 상품 목록의 정렬 조건을 적용한다.
     */
    private void sortProducts(
            List<Product> products,
            String sort
    ) {

        Comparator<Product> comparator;

        comparator = switch (sort) {
            case "salesDesc" ->
                    Comparator.comparing(
                            Product::getSalesCount,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    );

            case "priceAsc" ->
                    Comparator.comparing(
                            Product::getPrice,
                            Comparator.nullsLast(
                                    Comparator.naturalOrder()
                            )
                    );

            case "priceDesc" ->
                    Comparator.comparing(
                            Product::getPrice,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    );

            case "ratingDesc" ->
                    Comparator.comparing(
                            Product::getRating,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    );

            case "reviewCount" ->
                    Comparator.comparingLong(
                            (Product product) -> reviewRepository.countByProdNo(
                                    product.getProdNo()
                            )
                    ).reversed();

            default ->
                    Comparator.comparing(
                            Product::getCreatedAt,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()
                            )
                    );
        };

        products.sort(comparator);
    }


    @GetMapping("/product/search")
    public String search(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "false") boolean searchName,
                         @RequestParam(defaultValue = "false") boolean searchDescription,
                         @RequestParam(defaultValue = "false") boolean searchPrice,
                         @RequestParam(required = false) Integer minPrice,
                         @RequestParam(required = false) Integer maxPrice,
                         Model model) {

        List<Product> found;
        if (keyword.isBlank()) {
            found = List.of();
        } else if (searchName && !searchDescription) {
            // "상품명"만 체크한 경우
            found = productRepository.findByNameContainingIgnoreCase(keyword);
        } else if (searchDescription && !searchName) {
            // "설명"만 체크한 경우
            found = productRepository.findByDescriptionContainingIgnoreCase(keyword);
        } else {
            // 둘 다 체크했거나, 둘 다 안 체크한 기본 상태 -> 이름+설명 둘 다에서 검색
            found = productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        }

        // "가격" 체크박스를 켰을 때만 최소~최대 가격 필터 적용
        if (searchPrice) {
            if (minPrice != null) {
                found = found.stream()
                        .filter(p -> p.getPrice() != null && p.getPrice() >= minPrice)
                        .toList();
            }
            if (maxPrice != null) {
                found = found.stream()
                        .filter(p -> p.getPrice() != null && p.getPrice() <= maxPrice)
                        .toList();
            }
        }

        List<Map<String, Object>> products = found.stream()
                .map(this::toSearchResultMap)
                .toList();

        model.addAttribute("keyword", keyword);
        model.addAttribute("searchName", searchName);
        model.addAttribute("searchDescription", searchDescription);
        model.addAttribute("searchPrice", searchPrice);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("products", products);
        addProductLayout(model, null);

        return "product/search";
    }

    /**
     * product/search.html이 기대하는 Map 형태(name, description, isNew, isFreeShipping,
     * discount, originalPrice, price, seller)로 실제 Product 엔티티를 변환
     */
    private Map<String, Object> toSearchResultMap(Product p) {
        int discountRate = (p.getDiscountRate() == null) ? 0 : p.getDiscountRate();
        int price = (p.getPrice() == null) ? 0 : p.getPrice();
        int discountedPrice = price * (100 - discountRate) / 100;

        boolean isNew = p.getCreatedAt() != null
                && p.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(7));
        boolean isFreeShipping = p.getShippingFee() != null && p.getShippingFee() == 0;

        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("prodNo", p.getProdNo());
        map.put("name", p.getName());
        map.put("description", p.getDescription());
        map.put("thumb1", p.getThumb1());
        map.put("isNew", isNew);
        map.put("isFreeShipping", isFreeShipping);
        map.put("discount", discountRate);
        map.put("originalPrice", String.format("%,d", price));
        map.put("price", String.format("%,d", discountedPrice));
        map.put("seller", "상점 No." + p.getShopNo());
        return map;
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
        model.addAttribute("category", category);
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
        // 리뷰 저장
        reviewRepository.save(review);

        // 새 리뷰를 포함하여 해당 상품의 평균 별점을 다시 계산
        productService.updateProductRating(prodNo);

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

    private void addProductLayout(Model model, Integer selectedCateNo) {
        model.addAttribute("categories", categoryRepository.findByParentNoIsNull());
        model.addAttribute("rankingProducts", productRepository.findTop3ByOrderBySalesCountDesc());

        Category selected = (selectedCateNo == null)
                ? null
                : categoryRepository.findById(selectedCateNo).orElse(null);

        Integer mainCateNo = null;
        String mainCateName = null;
        Integer subCateNo = null;
        String subCateName = null;
        List<Category> subCategories = List.of();

        if (selected != null) {
            if (selected.getParentNo() == null) {
                // 대분류를 선택한 경우 (예: 패션)
                mainCateNo = selected.getCateNo();
                mainCateName = selected.getName();
                subCategories = categoryRepository.findByParentNo(mainCateNo);
            } else {
                // 소분류를 선택한 경우 (예: 상의)
                subCateNo = selected.getCateNo();
                subCateName = selected.getName();

                Category parent = categoryRepository.findById(selected.getParentNo()).orElse(null);
                if (parent != null) {
                    mainCateNo = parent.getCateNo();
                    mainCateName = parent.getName();
                    subCategories = categoryRepository.findByParentNo(mainCateNo);
                }
            }
        }

        model.addAttribute("mainCateNo", mainCateNo);
        model.addAttribute("mainCateName", mainCateName);
        model.addAttribute("subCateNo", subCateNo);
        model.addAttribute("subCateName", subCateName);
        model.addAttribute("subCategories", subCategories);
    private CartItemViewDTO toCartItemView(Cart cart) {
        Product product = productRepository.findById(cart.getProdNo())
                .orElseThrow(() -> new IllegalStateException("장바구니 상품이 존재하지 않습니다: " + cart.getProdNo()));
        int price = product.getPrice() == null ? 0 : product.getPrice();
        int discountRate = product.getDiscountRate() == null ? 0 : product.getDiscountRate();
        int discountedPrice = (int) (price * (100L - discountRate) / 100L);
        int shippingFee = product.getShippingFee() == null ? 0 : product.getShippingFee();
        int rewardPoints = product.getRewardPoints() == null ? 0 : product.getRewardPoints();

        return CartItemViewDTO.builder()
                .cartNo(cart.getCartNo())
                .prodNo(cart.getProdNo())
                .name(product.getName())
                .description(product.getDescription())
                .thumb1(product.getThumb1())
                .quantity(cart.getQuantity())
                .unitPrice(price)
                .discountRate(discountRate)
                .rewardPoints(rewardPoints)
                .shippingFee(shippingFee)
                .lineTotal(discountedPrice * cart.getQuantity() + shippingFee)
                .lineRewardPoints(rewardPoints * cart.getQuantity())
                .build();
    }

    private void addProductLayout(
            Model model,
            Integer mainCateNo
    ) {

        /*
         * 1차 카테고리 조회
         */
        List<Category> categories =
                categoryRepository.findByParentNoIsNull();

        /*
         * 1차 카테고리별 2차 카테고리 목록 구성
         */
        Map<Integer, List<Category>> subCategoryMap =
                new LinkedHashMap<>();

        for (Category category : categories) {
            subCategoryMap.put(
                    category.getCateNo(),
                    categoryRepository.findByParentNoOrderByCateNoAsc(
                            category.getCateNo()
                    )
            );
        }

        model.addAttribute("categories", categories);
        model.addAttribute("subCategoryMap", subCategoryMap);
        model.addAttribute("mainCateNo", mainCateNo);

        /*
         * 상품 페이지 사이드바 실시간 인기 상품
         */
        model.addAttribute(
                "rankingProducts",
                productRepository.findTop3ByOrderBySalesCountDesc()
        );
    }
}