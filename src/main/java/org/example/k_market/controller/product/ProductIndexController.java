package org.example.k_market.controller.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.CartItemViewDTO;
import org.example.k_market.dto.CheckoutRequest;
import org.example.k_market.entity.Cart;
import org.example.k_market.entity.Category;
import org.example.k_market.entity.Coupon;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.Qna;
import org.example.k_market.entity.Review;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.CategoryRepository;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.OrderDetailsRepository;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.repository.ProductSkuRepository;
import org.example.k_market.repository.QnaRepository;
import org.example.k_market.repository.ReviewRepository;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.security.MyUserDetails;
import org.example.k_market.service.ProductService;
import org.example.k_market.service.CartService;
import org.example.k_market.service.CheckoutService;
import org.example.k_market.service.CouponIssuanceService;
import org.example.k_market.service.aws.S3Uploader;
import org.example.k_market.service.admin.BannerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Log4j2
@RequiredArgsConstructor // Repository 자동 주입을 위해 필수!
@RequestMapping("/product")
public class ProductIndexController {

    private final CategoryRepository categoryRepository; // 이미 있을 가능성 높음
    private final ProductRepository productRepository;   // 탭에 열려있던 그거
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final ProductSkuRepository productSkuRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;     // 신규 주입
    private final QnaRepository qnaRepository;           // 신규 주입
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductService productService;
    private final S3Uploader s3Uploader;
    private final BannerService bannerService;
    private final ShopRepository shopRepository;
    private final CouponIssuanceService couponIssuanceService;

    private static final int PAGE_SIZE = 10;
    private static final int REVIEW_PAGE_SIZE = 5;
    private static final long REVIEW_IMAGE_MAX_BYTES = 5L * 1024 * 1024;

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

        List<Product> visibleProducts = productRepository.findAllVisible();
        List<Product> products;
        Integer activeCateNo = null;

        /*
         * 2차 카테고리 선택
         * 예: 아우터(cateNo=2), 상의(cateNo=3)
         */
        if (cateNo != null) {
            products = new ArrayList<>(
                    visibleProducts.stream().filter(product -> cateNo.equals(product.getCateNo())).toList()
            );

            activeCateNo = cateNo;

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
                        visibleProducts.stream().filter(product -> parentCateNo.equals(product.getCateNo())).toList()
                );
            } else {
                products = new ArrayList<>(
                        visibleProducts.stream().filter(product -> childCateNos.contains(product.getCateNo())).toList()
                );
            }

            activeCateNo = parentCateNo;

            /*
             * 카테고리 조건이 없으면 전체 상품 조회
             */
        } else {
            products = new ArrayList<>(visibleProducts);
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
        model.addAttribute("shopNames", shopNames(pageProducts));

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
                            this::getDiscountedPrice,
                            Comparator.nullsLast(
                                    Comparator.naturalOrder()
                            )
                    );

            case "priceDesc" ->
                    Comparator.comparing(
                            this::getDiscountedPrice,
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

    private Integer getDiscountedPrice(Product product) {
        if (product == null || product.getPrice() == null) {
            return null;
        }
        int discountRate = product.getDiscountRate() == null ? 0 : product.getDiscountRate();
        discountRate = Math.max(0, Math.min(discountRate, 100));
        return (int) (product.getPrice() * (100L - discountRate) / 100L);
    }

    private Map<Integer, String> shopNames(List<Product> products) {
        Map<Integer, String> names = new LinkedHashMap<>();
        products.stream().map(Product::getShopNo).filter(Objects::nonNull).distinct()
                .forEach(shopNo -> names.put(shopNo, shopRepository.findByShopNo(shopNo)
                        .map(Shop::getName)
                        .orElse("판매자 정보 없음")));
        return names;
    }

    private Map<Long, String> maskedReviewWriterNames(List<Review> reviews) {
        Map<Integer, String> memberNames = memberRepository.findAllById(
                        reviews.stream().map(Review::getMemberNo).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Member::getMemberNo, member -> maskName(member.getName())));

        Map<Long, String> writers = new LinkedHashMap<>();
        reviews.forEach(review -> writers.put(
                review.getReviewNO(), memberNames.getOrDefault(review.getMemberNo(), "회원")));
        return writers;
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) return "회원";
        String trimmed = name.trim();
        int characterCount = trimmed.codePointCount(0, trimmed.length());
        if (characterCount <= 1) return trimmed;
        int firstCharacterEnd = trimmed.offsetByCodePoints(0, 1);
        return trimmed.substring(0, firstCharacterEnd) + "*".repeat(characterCount - 1);
    }


    @GetMapping("/search")
    public String search(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                         @RequestParam(defaultValue = "false") boolean searchName,
                         @RequestParam(defaultValue = "false") boolean searchDescription,
                         @RequestParam(defaultValue = "false") boolean searchPrice,
                         @RequestParam(required = false) Integer minPrice,
                         @RequestParam(required = false) Integer maxPrice,
                         @RequestParam(defaultValue = "salesDesc") String sort,
                         Model model) {

        List<Product> visibleProducts = productRepository.findAllVisible();
        List<Product> found;
        if (keyword.isBlank()) {
            found = new ArrayList<>(visibleProducts);
        } else if (searchName && !searchDescription) {
            // "상품명"만 체크한 경우
            String loweredKeyword = keyword.toLowerCase();
            found = visibleProducts.stream()
                    .filter(product -> product.getName() != null
                            && product.getName().toLowerCase().contains(loweredKeyword))
                    .toList();
        } else if (searchDescription && !searchName) {
            // "설명"만 체크한 경우
            String loweredKeyword = keyword.toLowerCase();
            found = visibleProducts.stream()
                    .filter(product -> product.getDescription() != null
                            && product.getDescription().toLowerCase().contains(loweredKeyword))
                    .toList();
        } else {
            // 둘 다 체크했거나, 둘 다 안 체크한 기본 상태 -> 이름+설명 둘 다에서 검색
            String loweredKeyword = keyword.toLowerCase();
            found = visibleProducts.stream()
                    .filter(product -> (product.getName() != null
                            && product.getName().toLowerCase().contains(loweredKeyword))
                            || (product.getDescription() != null
                            && product.getDescription().toLowerCase().contains(loweredKeyword)))
                    .toList();
        }

        // "가격" 체크박스를 켰을 때만 최소~최대 가격 필터 적용
        if (searchPrice) {
            if (minPrice != null) {
                found = found.stream()
                        .filter(p -> getDiscountedPrice(p) != null && getDiscountedPrice(p) >= minPrice)
                        .toList();
            }
            if (maxPrice != null) {
                found = found.stream()
                        .filter(p -> getDiscountedPrice(p) != null && getDiscountedPrice(p) <= maxPrice)
                        .toList();
            }
        }

        String normalizedSort = switch (sort) {
            case "salesDesc", "priceAsc", "priceDesc", "ratingDesc", "reviewCount", "latest" -> sort;
            default -> "salesDesc";
        };
        found = new ArrayList<>(found);
        sortProducts(found, normalizedSort);

        List<Map<String, Object>> products = found.stream()
                .map(this::toSearchResultMap)
                .toList();

        model.addAttribute("keyword", keyword);
        model.addAttribute("searchName", searchName);
        model.addAttribute("searchDescription", searchDescription);
        model.addAttribute("searchPrice", searchPrice);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", normalizedSort);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("products", products);
        model.addAttribute("shopNameMap", productService.getShopNameMap());
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
        int discountedPrice = getDiscountedPrice(p) == null ? 0 : getDiscountedPrice(p);

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
        map.put("seller", shopRepository.findByShopNo(p.getShopNo())
                .map(Shop::getName)
                .orElse("판매자 정보 없음"));
        return map;
    }

    @GetMapping("/view")
    public String view(@RequestParam Integer prodNo,
                       @RequestParam(defaultValue = "1") int reviewPage,
                       Model model,
                       @AuthenticationPrincipal MyUserDetails userDetails) {
        Product product = productRepository.findVisibleById(Long.valueOf(prodNo)).orElse(null);
        if (product == null) {
            return "redirect:/product/list";
        }
        String shopName = shopRepository.findByShopNo(product.getShopNo())
                .map(Shop::getName)
                .orElse("판매자 정보 없음");
        Double sellerAverageRating = reviewRepository.findAverageRatingByShopNo(product.getShopNo());

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

        // 상품후기 목록 (최신순, 페이지당 5개)
        int requestedReviewPage = Math.max(reviewPage, 1) - 1;
        Page<Review> reviewPageData = reviewRepository.findByProdNoOrderByCreatedAtDesc(
                product.getProdNo(), PageRequest.of(requestedReviewPage, REVIEW_PAGE_SIZE));
        if (reviewPageData.getTotalPages() > 0 && requestedReviewPage >= reviewPageData.getTotalPages()) {
            requestedReviewPage = reviewPageData.getTotalPages() - 1;
            reviewPageData = reviewRepository.findByProdNoOrderByCreatedAtDesc(
                    product.getProdNo(), PageRequest.of(requestedReviewPage, REVIEW_PAGE_SIZE));
        }
        List<Review> reviewList = reviewPageData.getContent();
        Map<Long, String> reviewWriterNames = maskedReviewWriterNames(reviewList);

        // 상품 Q&A 목록 (해당 상품에 달린 문의 원글만, parentNo=0)
        List<Qna> qnaList = qnaRepository.findByProdNoAndParentNoOrderByNoDesc(product.getProdNo(), 0);

        int reviewMemberNo = userDetails == null ? 0 : userDetails.getUser().getMemberNo();
        boolean alreadyReviewed = userDetails != null
                && reviewRepository.existsByMemberNoAndProdNo(reviewMemberNo, product.getProdNo());
        boolean reviewEligible = userDetails != null
                && !alreadyReviewed
                && orderDetailsRepository.existsReviewablePurchase(reviewMemberNo, product.getProdNo());

        model.addAttribute("product", product);
        model.addAttribute("shopName", shopName);
        model.addAttribute("sellerAverageRating", sellerAverageRating);
        model.addAttribute("productSkus", productSkuRepository.findByProdNoOrderBySkuNoAsc(product.getProdNo()));
        model.addAttribute("category", category);
        model.addAttribute("parentCategory", parentCategory);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("reviewWriterNames", reviewWriterNames);
        model.addAttribute("reviewCurrentPage", requestedReviewPage + 1);
        model.addAttribute("reviewTotalPages", reviewPageData.getTotalPages());
        model.addAttribute("reviewTotalElements", reviewPageData.getTotalElements());
        Coupon productCoupon = couponIssuanceService.findActiveProductCoupon(product.getProdNo());
        model.addAttribute("productCoupon", productCoupon);
        model.addAttribute("productDetailBanner", productCoupon == null
                ? null : bannerService.getDisplayableBanner("PRODUCT1"));
        model.addAttribute("qnaList", qnaList);
        model.addAttribute("reviewEligible", reviewEligible);
        model.addAttribute("reviewEligibilityMessage", userDetails == null
                ? "로그인 후 배송준비 이상의 구매 상품에만 후기를 작성할 수 있습니다."
                : alreadyReviewed
                  ? "이미 이 상품의 후기를 작성했습니다. 후기는 상품별로 한 번만 작성할 수 있습니다."
                  : "배송준비, 배송중, 배송완료 또는 구매확정 상태의 구매 내역이 있어야 후기를 작성할 수 있습니다.");
        addProductLayout(model, mainCateNo);
        return "product/view";
    }

    /**
     * 장바구니 담기
     * - 로그인 안 되어 있으면 로그인 페이지로 보냄
     * - 같은 상품과 같은 SKU가 이미 있으면 기존 행의 수량에 합산
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long prodNo,
                            @RequestParam(required = false) Long skuNo,
                            @RequestParam(defaultValue = "1") int quantity,
                            @AuthenticationPrincipal MyUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }
        try {
            cartService.add(userDetails.getUser().getMemberNo(), prodNo, skuNo, quantity);
            redirectAttributes.addFlashAttribute("cartMessage", "장바구니에 상품을 담았습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/product/view?prodNo=" + prodNo;
        }
        return "redirect:/product/cart";
    }

    @PostMapping("/cart/add-selected")
    public String addSelectedToCart(@RequestParam Long prodNo,
                                    @RequestParam(required = false) List<String> selectedSkuNos,
                                    @RequestParam(required = false) List<Integer> quantities,
                                    @AuthenticationPrincipal MyUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }
        try {
            if (selectedSkuNos == null || quantities == null
                    || selectedSkuNos.isEmpty() || selectedSkuNos.size() != quantities.size()) {
                throw new IllegalArgumentException("장바구니에 담을 옵션을 선택해주세요.");
            }
            List<CartService.Selection> selections = new ArrayList<>();
            for (int i = 0; i < selectedSkuNos.size(); i++) {
                String skuValue = selectedSkuNos.get(i);
                Long skuNo = "none".equals(skuValue) ? null : Long.valueOf(skuValue);
                selections.add(new CartService.Selection(skuNo, quantities.get(i)));
            }
            cartService.addSelected(userDetails.getUser().getMemberNo(), prodNo, selections);
            redirectAttributes.addFlashAttribute("cartMessage", "선택한 상품을 장바구니에 담았습니다.");
            return "redirect:/product/cart";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/product/view?prodNo=" + prodNo;
        }
    }

    /**
     * 바로구매 - 결제 페이지(주문서)로 이동
     * (실제 결제/주문 저장 로직은 별도 OrderController에서 추가 구현 필요)
     */
    @PostMapping("/order/direct")
    public String orderDirect(@RequestParam Long prodNo,
                              @RequestParam(required = false) Long skuNo,
                              @RequestParam(defaultValue = "1") int quantity,
                              @RequestParam(required = false) List<String> selectedSkuNos,
                              @RequestParam(required = false) List<Integer> quantities,
                              Model model,
                              @AuthenticationPrincipal MyUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        try {
            List<String> directSkuNos = selectedSkuNos;
            List<Integer> directQuantities = quantities;
            if (directSkuNos == null || directSkuNos.isEmpty()) {
                directSkuNos = List.of(skuNo == null ? "none" : String.valueOf(skuNo));
                directQuantities = List.of(quantity);
            }
            if (directQuantities == null || directSkuNos.size() != directQuantities.size()) {
                throw new IllegalArgumentException("바로구매할 상품 옵션을 다시 선택해주세요.");
            }

            List<CartItemViewDTO> items = new ArrayList<>();
            for (int i = 0; i < directSkuNos.size(); i++) {
                String skuValue = directSkuNos.get(i);
                Long selectedSkuNo = "none".equals(skuValue) ? null : Long.valueOf(skuValue);
                items.add(cartService.previewProduct(prodNo, selectedSkuNo, directQuantities.get(i))
                        .toBuilder()
                        .itemKey("direct:" + i)
                        .build());
            }
            addOrderModel(model, items, userDetails.getUser().getMemberNo());
            model.addAttribute("directProdNo", prodNo);
            model.addAttribute("directSkuNos", directSkuNos);
            model.addAttribute("directQuantities", directQuantities);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/product/view?prodNo=" + prodNo;
        }
        return "product/order";
    }

    @GetMapping("/cart")
    public String cart(Model model, @AuthenticationPrincipal MyUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        List<CartItemViewDTO> cartItems = cartService.getItems(userDetails.getUser().getMemberNo());

        int totalQuantity = cartItems.stream().mapToInt(CartItemViewDTO::getQuantity).sum();
        int totalProductPrice = cartItems.stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        int totalShippingFee = groupedShippingFee(cartItems);
        int totalOrderPrice = cartItems.stream().mapToInt(CartItemViewDTO::getLineTotal).sum()
                + totalShippingFee;
        int totalRewardPoints = cartItems.stream().mapToInt(CartItemViewDTO::getLineRewardPoints).sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("totalProductPrice", totalProductPrice);
        model.addAttribute("totalDiscount", totalProductPrice + totalShippingFee - totalOrderPrice);
        model.addAttribute("totalShippingFee", totalShippingFee);
        model.addAttribute("totalOrderPrice", totalOrderPrice);
        model.addAttribute("totalRewardPoints", totalRewardPoints);
        addProductLayout(model, null);
        return "product/cart";
    }

    @GetMapping("/order")
    public String order() {
        return "redirect:/product/cart";
    }

    @PostMapping("/cart/quantity")
    public String updateCartQuantity(@RequestParam long cartNo,
                                     @RequestParam int quantity,
                                     @AuthenticationPrincipal MyUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/member/login";
        try {
            cartService.updateQuantity(userDetails.getUser().getMemberNo(), cartNo, quantity);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/product/cart";
    }

    @PostMapping("/cart/delete")
    public String deleteCartItems(@RequestParam(required = false) List<Long> cartNos,
                                  @AuthenticationPrincipal MyUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/member/login";
        try {
            cartService.deleteSelected(userDetails.getUser().getMemberNo(), cartNos);
            redirectAttributes.addFlashAttribute("cartMessage", "선택한 상품을 삭제했습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
        }
        return "redirect:/product/cart";
    }

    @PostMapping("/cart/delete-sold-out")
    public String deleteSoldOutCartItems(@AuthenticationPrincipal MyUserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/member/login";
        int deleted = cartService.deleteSoldOut(userDetails.getUser().getMemberNo());
        redirectAttributes.addFlashAttribute("cartMessage",
                deleted == 0 ? "삭제할 품절 상품이 없습니다." : "품절 상품 " + deleted + "개를 삭제했습니다.");
        return "redirect:/product/cart";
    }

    @PostMapping("/order/cart")
    public String orderCartItems(@RequestParam(required = false) List<Long> cartNos,
                                 @AuthenticationPrincipal MyUserDetails userDetails,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/member/login";
        try {
            List<CartItemViewDTO> items = cartService.getSelectedItems(userDetails.getUser().getMemberNo(), cartNos);
            addOrderModel(model, items, userDetails.getUser().getMemberNo());
            model.addAttribute("selectedCartNos", cartNos);
            return "product/order";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/product/cart";
        }
    }

    @PostMapping("/order/complete")
    public String completeOrder(@ModelAttribute CheckoutRequest checkoutRequest,
                                @AuthenticationPrincipal MyUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/member/login";
        try {
            CheckoutService.CheckoutResult result = checkoutService.placeOrder(
                    userDetails.getUser().getMemberNo(), checkoutRequest);
            redirectAttributes.addAttribute("orderNo", result.orderNo());
            return "redirect:/product/order/complete";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            if (checkoutRequest.isCartOrder()) {
                return "redirect:/product/cart";
            }
            Long prodNo = checkoutRequest.getDirectProdNo();
            return prodNo == null ? "redirect:/product/cart" : "redirect:/product/view?prodNo=" + prodNo;
        }
    }

    @GetMapping("/order/complete")
    public String orderComplete(@RequestParam int orderNo,
                                @AuthenticationPrincipal MyUserDetails userDetails,
                                Model model) {
        if (userDetails == null) return "redirect:/member/login";
        try {
            model.addAttribute("receipt", checkoutService.getReceipt(
                    userDetails.getUser().getMemberNo(), orderNo));
            addProductLayout(model, null);
            return "product/order-complete";
        } catch (IllegalArgumentException e) {
            return "redirect:/my/order";
        }
    }

    /**
     * 상품후기 등록
     */
    @PostMapping("/review/add")
    public String addReview(@RequestParam Long prodNo,
                            @RequestParam int rating,
                            @RequestParam String content,
                            @RequestParam(required = false) List<MultipartFile> images,
                            @AuthenticationPrincipal MyUserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        int memberNo = userDetails.getUser().getMemberNo();
        if (!orderDetailsRepository.existsReviewablePurchase(memberNo, prodNo)) {
            return "redirect:/product/view?prodNo=" + prodNo + "#reviews";
        }
        if (reviewRepository.existsByMemberNoAndProdNo(memberNo, prodNo)) {
            return "redirect:/product/view?prodNo=" + prodNo + "#reviews";
        }

        if (rating < 1 || rating > 5 || content == null || content.isBlank()) {
            redirectAttributes.addFlashAttribute("reviewError", "평점과 후기 내용을 확인해주세요.");
            return "redirect:/product/view?prodNo=" + prodNo + "#reviews";
        }

        List<String> imageUrls;
        try {
            imageUrls = uploadReviewImages(images);
        } catch (IllegalArgumentException | IOException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
            return "redirect:/product/view?prodNo=" + prodNo + "#reviews";
        }

        Review review = Review.builder()
                .prodNo(prodNo)
                .memberNo(memberNo)
                .rating(rating)
                .content(content.trim())
                .imageUrl1(imageUrls.size() > 0 ? imageUrls.get(0) : null)
                .imageUrl2(imageUrls.size() > 1 ? imageUrls.get(1) : null)
                .imageUrl3(imageUrls.size() > 2 ? imageUrls.get(2) : null)
                .createdAt(LocalDateTime.now())
                .build();
        // 리뷰 저장
        reviewRepository.save(review);

        // 새 리뷰를 포함하여 해당 상품의 평균 별점을 다시 계산
        productService.updateProductRating(prodNo);

        return "redirect:/product/view?prodNo=" + prodNo + "&reviewPage=1#reviews";
    }

    private List<String> uploadReviewImages(List<MultipartFile> images) throws IOException {
        List<MultipartFile> files = images == null ? List.of() : images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
        if (files.size() > 3) {
            throw new IllegalArgumentException("후기 사진은 최대 3장까지 등록할 수 있습니다.");
        }

        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file.getSize() > REVIEW_IMAGE_MAX_BYTES) {
                    throw new IllegalArgumentException("후기 사진은 한 장당 5MB 이하여야 합니다.");
                }
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("이미지 파일만 후기 사진으로 등록할 수 있습니다.");
                }
                uploadedUrls.add(s3Uploader.upload(file, "reviews"));
            }
            return uploadedUrls;
        } catch (IllegalArgumentException | IOException e) {
            uploadedUrls.forEach(s3Uploader::deleteFile);
            throw e;
        }
    }

    private void addOrderModel(Model model, List<CartItemViewDTO> items, int memberNo) {
        int totalQuantity = items.stream().mapToInt(CartItemViewDTO::getQuantity).sum();
        int totalProductPrice = items.stream().mapToInt(item -> item.getUnitPrice() * item.getQuantity()).sum();
        int totalShippingFee = groupedShippingFee(items);
        int totalOrderPrice = items.stream().mapToInt(CartItemViewDTO::getLineTotal).sum()
                + totalShippingFee;
        int totalRewardPoints = items.stream().mapToInt(CartItemViewDTO::getLineRewardPoints).sum();
        Map<Long, Integer> productPrices = new LinkedHashMap<>();
        items.forEach(item -> {
            int discountRate = Math.max(0, Math.min(item.getDiscountRate(), 100));
            int discountedUnitPrice = (int) (item.getUnitPrice() * (100L - discountRate) / 100L);
            productPrices.merge(item.getProdNo(), discountedUnitPrice * item.getQuantity(), Integer::sum);
        });

        model.addAttribute("orderItems", items);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("totalProductPrice", totalProductPrice);
        model.addAttribute("totalShippingFee", totalShippingFee);
        model.addAttribute("totalDiscount", totalProductPrice + totalShippingFee - totalOrderPrice);
        model.addAttribute("totalOrderPrice", totalOrderPrice);
        model.addAttribute("totalRewardPoints", totalRewardPoints);
        model.addAttribute("orderMember", memberRepository.findById(memberNo).orElse(null));
        model.addAttribute("availableCoupons", couponIssuanceService.availableForOrder(
                memberNo, productPrices, totalShippingFee));
        addProductLayout(model, null);
    }

    private int groupedShippingFee(List<CartItemViewDTO> items) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        CartItemViewDTO::getShopNo,
                        Collectors.mapping(CartItemViewDTO::getShippingFee,
                                Collectors.maxBy(Integer::compareTo))))
                .values().stream()
                .flatMap(Optional::stream)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private void addProductLayout(Model model, Integer selectedCateNo) {
        model.addAttribute("categories", categoryRepository.findByParentNoIsNull());
        model.addAttribute("rankingProducts", productService.getBestProducts());

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
                subCategories = categoryRepository.findByParentNoOrderByCateNoAsc(mainCateNo);
            } else {
                // 소분류를 선택한 경우 (예: 상의)
                subCateNo = selected.getCateNo();
                subCateName = selected.getName();

                Category parent = categoryRepository.findById(selected.getParentNo()).orElse(null);
                if (parent != null) {
                    mainCateNo = parent.getCateNo();
                    mainCateName = parent.getName();
                    subCategories = categoryRepository.findByParentNoOrderByCateNoAsc(mainCateNo);
                }
            }
        }

        model.addAttribute("mainCateNo", mainCateNo);
        model.addAttribute("mainCateName", mainCateName);
        model.addAttribute("subCateNo", subCateNo);
        model.addAttribute("subCateName", subCateName);
        model.addAttribute("subCategories", subCategories);
        addProductSidebarMap(model, mainCateNo);
    }

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

    private void addProductSidebarMap(
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
                productService.getBestProducts()
        );
    }
}
