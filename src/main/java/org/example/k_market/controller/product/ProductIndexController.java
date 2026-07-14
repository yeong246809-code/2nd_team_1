package org.example.k_market.controller.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.CartItemViewDTO;
import org.example.k_market.dto.CheckoutRequest;
import org.example.k_market.entity.Category;
import org.example.k_market.entity.Member;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.Qna;
import org.example.k_market.entity.Review;
import org.example.k_market.repository.CategoryRepository;
import org.example.k_market.repository.MemberRepository;
import org.example.k_market.repository.OrderDetailsRepository;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.repository.ProductSkuRepository;
import org.example.k_market.repository.QnaRepository;
import org.example.k_market.repository.ReviewRepository;
import org.example.k_market.security.MyUserDetails;
import org.example.k_market.service.ProductService;
import org.example.k_market.service.CartService;
import org.example.k_market.service.CheckoutService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor // Repository 자동 주입을 위해 필수!
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
    public String search(
            @RequestParam(name = "keyword", required = false, defaultValue = "")
            String keyword,
            Model model
    ) {

        // 검색어 앞뒤의 공백 제거
        String trimmedKeyword = keyword.trim();

        // ProductService를 통해 실제 DB 상품 검색
        List<Product> products =
                productService.searchProducts(trimmedKeyword);

        // 검색 결과 화면에 필요한 데이터 전달
        model.addAttribute("keyword", trimmedKeyword);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("products", products);

        // 상품 화면 공통 레이아웃 정보 전달
        addProductLayout(model, null);

        return "product/search";
    }

    @GetMapping("/product/view")
    public String view(@RequestParam Integer prodNo,
                       Model model,
                       @AuthenticationPrincipal MyUserDetails userDetails) {
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

        int reviewMemberNo = userDetails == null ? 0 : userDetails.getUser().getMemberNo();
        boolean alreadyReviewed = userDetails != null
                && reviewRepository.existsByMemberNoAndProdNo(reviewMemberNo, product.getProdNo());
        boolean reviewEligible = userDetails != null
                && !alreadyReviewed
                && orderDetailsRepository.existsReviewablePurchase(reviewMemberNo, product.getProdNo());

        model.addAttribute("product", product);
        model.addAttribute("productSkus", productSkuRepository.findByProdNoOrderBySkuNoAsc(product.getProdNo()));
        model.addAttribute("parentCategory", parentCategory);
        model.addAttribute("reviewList", reviewList);
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
    @PostMapping("/product/cart/add")
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

    /**
     * 바로구매 - 결제 페이지(주문서)로 이동
     * (실제 결제/주문 저장 로직은 별도 OrderController에서 추가 구현 필요)
     */
    @PostMapping("/product/order/direct")
    public String orderDirect(@RequestParam Long prodNo,
                              @RequestParam(required = false) Long skuNo,
                              @RequestParam(defaultValue = "1") int quantity,
                              Model model,
                              @AuthenticationPrincipal MyUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        try {
            CartItemViewDTO item = cartService.previewProduct(prodNo, skuNo, quantity);
            addOrderModel(model, List.of(item), userDetails.getUser().getMemberNo());
            model.addAttribute("directProdNo", prodNo);
            model.addAttribute("directSkuNo", skuNo);
            model.addAttribute("directQuantity", quantity);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("cartError", e.getMessage());
            return "redirect:/product/view?prodNo=" + prodNo;
        }
        return "product/order";
    }

    @GetMapping("/product/cart")
    public String cart(Model model, @AuthenticationPrincipal MyUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }

        List<CartItemViewDTO> cartItems = cartService.getItems(userDetails.getUser().getMemberNo());

        int totalQuantity = cartItems.stream().mapToInt(CartItemViewDTO::getQuantity).sum();
        int totalProductPrice = cartItems.stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        int totalShippingFee = cartItems.stream().mapToInt(CartItemViewDTO::getShippingFee).sum();
        int totalOrderPrice = cartItems.stream().mapToInt(CartItemViewDTO::getLineTotal).sum();
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

    @GetMapping("/product/order")
    public String order() {
        return "redirect:/product/cart";
    }

    @PostMapping("/product/cart/quantity")
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

    @PostMapping("/product/cart/delete")
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

    @PostMapping("/product/cart/delete-sold-out")
    public String deleteSoldOutCartItems(@AuthenticationPrincipal MyUserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/member/login";
        int deleted = cartService.deleteSoldOut(userDetails.getUser().getMemberNo());
        redirectAttributes.addFlashAttribute("cartMessage",
                deleted == 0 ? "삭제할 품절 상품이 없습니다." : "품절 상품 " + deleted + "개를 삭제했습니다.");
        return "redirect:/product/cart";
    }

    @PostMapping("/product/order/cart")
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

    @PostMapping("/product/order/complete")
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

    @GetMapping("/product/order/complete")
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
    @PostMapping("/product/review/add")
    public String addReview(@RequestParam Long prodNo,
                            @RequestParam int rating,
                            @RequestParam String content,
                            @AuthenticationPrincipal MyUserDetails userDetails) {
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

        Review review = Review.builder()
                .prodNo(prodNo)
                .memberNo(memberNo)
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

    private void addOrderModel(Model model, List<CartItemViewDTO> items, int memberNo) {
        int totalQuantity = items.stream().mapToInt(CartItemViewDTO::getQuantity).sum();
        int totalProductPrice = items.stream().mapToInt(item -> item.getUnitPrice() * item.getQuantity()).sum();
        int totalShippingFee = items.stream().mapToInt(CartItemViewDTO::getShippingFee).sum();
        int totalOrderPrice = items.stream().mapToInt(CartItemViewDTO::getLineTotal).sum();
        int totalRewardPoints = items.stream().mapToInt(CartItemViewDTO::getLineRewardPoints).sum();

        model.addAttribute("orderItems", items);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("totalProductPrice", totalProductPrice);
        model.addAttribute("totalShippingFee", totalShippingFee);
        model.addAttribute("totalDiscount", totalProductPrice + totalShippingFee - totalOrderPrice);
        model.addAttribute("totalOrderPrice", totalOrderPrice);
        model.addAttribute("totalRewardPoints", totalRewardPoints);
        model.addAttribute("orderMember", memberRepository.findById(memberNo).orElse(null));
        addProductLayout(model, null);
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
