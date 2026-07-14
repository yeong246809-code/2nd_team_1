package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dao.CategoryDAO;
import org.example.k_market.dto.CategoryDTO;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.dto.ProductDTO;
import org.example.k_market.entity.*;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.security.MyUserDetails;
import org.example.k_market.service.ProductOptionService;
import org.example.k_market.service.ProductService;
import org.example.k_market.service.admin.ProductSkuService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {

    private final ProductService productService;
    private final ProductOptionService productOptionService;
    private final ProductSkuService productSkuService; // 🚨 Repository 직접 호출 대신 Service 사용
    private final CategoryDAO categoryDAO;
    private final ShopRepository shopRepository;

    @GetMapping("/list")
    public String list(
            Model model,
            Authentication authentication,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", defaultValue = "latest") String sort,
            @RequestParam(value = "page", defaultValue = "1") int page) {

        // 관리자가 아니면 로그인한 판매자의 shopNo로 제한
        Integer filterShopNo = isAdmin(authentication) ? null : currentShopNo(authentication).orElse(-1);

        // 페이징, 검색, 정렬된 목록 조회 (한 페이지당 10개씩)
        PageResponseDTO<ProductDTO> pageResponse = productService.getProductsPaged(
                filterShopNo, searchType, keyword, sort, page, 10
        );

        // 상호명 매핑을 위한 Map 전달
        model.addAttribute("shopNameMap", productService.getShopNameMap());
        model.addAttribute("pageResponse", pageResponse);
        model.addAttribute("productList", pageResponse.getDtoList());
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);

        return "admin/product/list";
    }

    @GetMapping("/register")
    public String register(Model model, Authentication authentication) {
        List<CategoryDTO> allCategories = categoryDAO.findAll();
        Optional<Shop> currentShop = findCurrentUserShop(authentication);

        if (!model.containsAttribute("product")) {
            ProductDTO product = new ProductDTO();
            if (!isAdmin(authentication)) {
                currentShop.map(Shop::getShopNo).ifPresent(product::setShopNo);
            }
            model.addAttribute("product", product);
        }

        model.addAttribute("allCategories", allCategories);
        model.addAttribute("shops", shopRepository.findAll());
        model.addAttribute("adminUser", isAdmin(authentication));
        model.addAttribute("currentShop", currentShop.orElse(null));

        return "admin/product/register";
    }

    /**
     * 상품 등록 처리 (옵션 및 SKU 모두 완벽 저장!)
     */
    @PostMapping("/register")
    public String registerSubmit(
            @ModelAttribute("product") ProductDTO productDTO,
            @RequestParam(value = "file1", required = false) MultipartFile file1,
            @RequestParam(value = "file2", required = false) MultipartFile file2,
            @RequestParam(value = "file3", required = false) MultipartFile file3,
            @RequestParam(value = "detailFile", required = false) MultipartFile detailFile,

            // 🚨 [추가됨] 이전 단계에서 html에 추가했던 optionNames, optionValues 파라미터 수신
            @RequestParam(value = "optionNames", required = false) List<String> optionNames,
            @RequestParam(value = "optionValues", required = false) List<String> optionValues,

            // SKU 조합 데이터 파라미터
            @RequestParam(value = "skuNames", required = false) List<String> skuNames,
            @RequestParam(value = "prices", required = false) List<Integer> prices,
            @RequestParam(value = "stocks", required = false) List<Integer> stocks,

            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        log.info("Option Names Received: {}", optionNames);
        log.info("SKU Names Received: {}", skuNames);

        try {
            validateProduct(productDTO, authentication);
            applyDefaults(productDTO);

            int discountPrice = productDTO.getPrice() - (productDTO.getPrice() * productDTO.getDiscountRate() / 100);
            productDTO.setRewardPoints((int) (discountPrice * 0.01));

            // 1. 상품 기본정보 저장
            Product savedProduct = productService.register(productDTO, file1, file2, file3, detailFile);

            // 2. 🚨 [추가됨] 기본 옵션(ProductOptions & Items) 저장 로직 호출!
            if (optionNames != null && !optionNames.isEmpty()) {
                productOptionService.saveOptions(savedProduct.getProdNo(), optionNames, optionValues);
            }

            // 3. SKU(조합형 옵션) 테이블 저장
            if (skuNames != null && !skuNames.isEmpty()) {
                List<ProductSkus> skuList = new ArrayList<>();
                for (int i = 0; i < skuNames.size(); i++) {
                    skuList.add(ProductSkus.builder()
                            .product(savedProduct)
                            .skuName(skuNames.get(i))
                            .price(prices != null && i < prices.size() && prices.get(i) != null ? prices.get(i) : 0)
                            .stock(stocks != null && i < stocks.size() && stocks.get(i) != null ? stocks.get(i) : 0)
                            .build());
                }
                productSkuService.saveAll(skuList); // Service를 통해 저장
            }

            redirectAttributes.addFlashAttribute("successMessage", "상품이 등록되었습니다.");
            return "redirect:/admin/product/list";

        } catch (IllegalArgumentException | IOException e) {
            log.error("상품 등록 실패", e);
            redirectAttributes.addFlashAttribute("productError", e.getMessage());
            redirectAttributes.addFlashAttribute("product", productDTO);
            return "redirect:/admin/product/register";
        }
    }

    @GetMapping("/modify/{prodNo}")
    public String modifyForm(@PathVariable("prodNo") Long prodNo, Model model, Authentication authentication) {
        Product product = productService.findById(prodNo);
        if (product == null) return "redirect:/admin/product/list";
        assertProductOwnership(product, authentication);

        List<ProductOptions> options = productOptionService.findOptionsByProdNo(prodNo);
        Map<Long, List<ProductOptionItems>> optionItemsMap = productOptionService.findOptionItemsMap(options);
        Map<Long, String> optionValueMap = createOptionValueMap(options, optionItemsMap);
        Optional<Shop> currentShop = findCurrentUserShop(authentication);

        model.addAttribute("product", product.toDTO());
        model.addAttribute("allCategories", categoryDAO.findAll());
        model.addAttribute("shops", shopRepository.findAll());
        model.addAttribute("adminUser", isAdmin(authentication));
        model.addAttribute("currentShop", currentShop.orElse(null));
        model.addAttribute("options", options);
        model.addAttribute("optionItemsMap", optionItemsMap);
        model.addAttribute("optionValueMap", optionValueMap);
        model.addAttribute("skuList", productSkuService.findByProdNo(prodNo));

        return "admin/product/modify";
    }

    @PostMapping("/modify")
    public String modifySubmit(
            @ModelAttribute("product") ProductDTO productDTO,
            BindingResult bindingResult,
            @RequestParam(value = "file1", required = false) MultipartFile file1,
            @RequestParam(value = "file2", required = false) MultipartFile file2,
            @RequestParam(value = "file3", required = false) MultipartFile file3,
            @RequestParam(value = "detailFile", required = false) MultipartFile detailFile,

            // 🚨 [수정] modify.html에서 변경된 파라미터명(optionNames, optionValues) 수신
            @RequestParam(value = "optionNames", required = false) List<String> optionNames,
            @RequestParam(value = "optionValues", required = false) List<String> optionValues,

            // 🚨 [추가] modify.html의 조합 테이블로부터 넘어온 SKU 파라미터 수신
            @RequestParam(value = "skuNames", required = false) List<String> skuNames,
            @RequestParam(value = "prices", required = false) List<Integer> prices,
            @RequestParam(value = "stocks", required = false) List<Integer> stocks,

            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.error("상품 수정 바인딩 오류");
            bindingResult.getAllErrors()
                    .forEach(error -> log.error(error.toString()));

            redirectAttributes.addFlashAttribute(
                    "productError",
                    "입력값을 다시 확인해 주세요."
            );

            return "redirect:/admin/product/modify/" + productDTO.getProdNo();
        }

        try {
            Product existingProduct = productService.findById(productDTO.getProdNo());
            if (existingProduct == null) {
                throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
            }
            assertProductOwnership(existingProduct, authentication);

            // 카테고리와 상점정보 검증 및 기본값 세팅
            validateProduct(productDTO, authentication);
            applyDefaults(productDTO);

            // 1. 상품 기본정보 및 대표/상세 이미지 교체 (S3 업로드 및 저장)
            Product modifiedProduct = productService.modify(
                    productDTO,
                    file1,
                    file2,
                    file3,
                    detailFile
            );

            // 2. 일반 옵션 교체 저장 (기존 DB 데이터 삭제 후 재생성)
            productOptionService.replaceOptions(
                    modifiedProduct.getProdNo(),
                    optionNames,
                    optionValues
            );

            // 3. 🚨 [추가] 조합형 SKU 옵션 교체 저장 (기존 DB 데이터 삭제 후 재생성)
            if (skuNames != null && !skuNames.isEmpty()) {
                List<ProductSkus> skuList = new java.util.ArrayList<>();
                for (int i = 0; i < skuNames.size(); i++) {
                    skuList.add(ProductSkus.builder()
                            .product(modifiedProduct) // 연관관계 매핑 객체 전달
                            .skuName(skuNames.get(i))
                            .price(prices != null && i < prices.size() && prices.get(i) != null ? prices.get(i) : 0)
                            .stock(stocks != null && i < stocks.size() && stocks.get(i) != null ? stocks.get(i) : 0)
                            .build());
                }
                // 기존 SKU를 전부 지우고 새로운 조합으로 Bulk Insert 수행
                productSkuService.replaceSkus(modifiedProduct.getProdNo(), skuList);
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "상품이 수정되었습니다."
            );

            return "redirect:/admin/product/list";

        } catch (IllegalArgumentException | IOException e) {
            log.error("상품 수정 실패 - prodNo: {}", productDTO.getProdNo(), e);

            redirectAttributes.addFlashAttribute(
                    "productError",
                    e.getMessage()
            );

            return "redirect:/admin/product/modify/" + productDTO.getProdNo();
        }
    }

    @PostMapping("/delete/{prodNo}")
    @ResponseBody
    public Map<String, Object> deleteProduct(@PathVariable("prodNo") Long prodNo, Authentication authentication) {
        try {
            Product product = productService.findById(prodNo);
            if (product == null) throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
            assertProductOwnership(product, authentication);

            productOptionService.deleteOptionsByProdNo(prodNo);
            productService.delete(prodNo);

            return Map.of("success", true, "message", "상품이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("상품 삭제 실패 - prodNo: {}", prodNo, e);
            return Map.of("success", false, "message", "상품 삭제에 실패했습니다.");
        }
    }

    @PostMapping("/deleteSelected")
    @ResponseBody
    public Map<String, Object> deleteSelectedProducts(@RequestBody List<Long> prodNos, Authentication authentication) {
        try {
            if (prodNos == null || prodNos.isEmpty()) throw new IllegalArgumentException("삭제할 상품을 선택해 주세요.");

            for (Long prodNo : prodNos) {
                Product product = productService.findById(prodNo);
                if (product == null) throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
                assertProductOwnership(product, authentication);
            }

            for (Long prodNo : prodNos) {
                productOptionService.deleteOptionsByProdNo(prodNo);
            }

            productService.deleteAll(prodNos);
            return Map.of("success", true, "message", "선택한 상품이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("상품 일괄 삭제 실패", e);
            return Map.of("success", false, "message", "상품 일괄 삭제에 실패했습니다.");
        }
    }

    private Map<Long, String> createOptionValueMap(List<ProductOptions> options, Map<Long, List<ProductOptionItems>> optionItemsMap) {
        Map<Long, String> optionValueMap = new LinkedHashMap<>();
        if (options == null || options.isEmpty()) return optionValueMap;

        for (ProductOptions option : options) {
            List<ProductOptionItems> items = optionItemsMap.getOrDefault(option.getOptionNo(), List.of());
            String optionValue = items.stream()
                    .map(ProductOptionItems::getItemName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.joining(", "));
            optionValueMap.put(option.getOptionNo(), optionValue);
        }
        return optionValueMap;
    }

    private void validateProduct(ProductDTO productDTO, Authentication authentication) {
        if (productDTO.getCateNo() == null) throw new IllegalArgumentException("상품 카테고리를 선택해 주세요.");
        if (categoryDAO.findById(productDTO.getCateNo()).isEmpty()) throw new IllegalArgumentException("선택한 카테고리가 존재하지 않습니다.");
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) throw new IllegalArgumentException("상품명을 입력해 주세요.");
        if (productDTO.getPrice() == null) throw new IllegalArgumentException("상품 가격을 입력해 주세요.");

        productDTO.setShopNo(resolveShopNo(productDTO.getShopNo(), authentication));
    }

    private Integer resolveShopNo(Integer requestedShopNo, Authentication authentication) {
        if (isAdmin(authentication)) {
            if (requestedShopNo == null) throw new IllegalArgumentException("상점을 선택해 주세요.");
            return shopRepository.findByShopNo(requestedShopNo).map(Shop::getShopNo)
                    .orElseThrow(() -> new IllegalArgumentException("선택한 상점이 존재하지 않습니다."));
        }

        Shop shop = findCurrentUserShop(authentication)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 판매자와 연결된 상점이 없습니다."));

        if (shop.getShopNo() == null) throw new IllegalArgumentException("연결된 상점 번호가 생성되지 않았습니다.");
        return shop.getShopNo();
    }

    private Optional<Shop> findCurrentUserShop(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails userDetails)) {
            return Optional.empty();
        }
        return shopRepository.findById(userDetails.getUser().getMemberNo());
    }

    private Optional<Integer> currentShopNo(Authentication authentication) {
        return findCurrentUserShop(authentication).map(Shop::getShopNo);
    }

    private void assertProductOwnership(Product product, Authentication authentication) {
        if (isAdmin(authentication)) return;
        Integer shopNo = currentShopNo(authentication)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("판매자 상점 정보를 찾을 수 없습니다."));
        if (!shopNo.equals(product.getShopNo())) {
            throw new org.springframework.security.access.AccessDeniedException("다른 판매자의 상품에는 접근할 수 없습니다.");
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void applyDefaults(ProductDTO productDTO) {
        productDTO.setName(productDTO.getName().trim());
        if (productDTO.getDiscountRate() == null) productDTO.setDiscountRate(0);
        int discountPrice = productDTO.getDiscountPrice();
        productDTO.setRewardPoints((int) (discountPrice * 0.01));
        if (productDTO.getStockQuantity() == null) productDTO.setStockQuantity(0);
        if (productDTO.getShippingFee() == null) productDTO.setShippingFee(0);
        if (productDTO.getViewCount() == null) productDTO.setViewCount(0);
        if (productDTO.getSalesCount() == null) productDTO.setSalesCount(0);
        if (productDTO.getStatus() == null) productDTO.setStatus("ACTIVE");
        if (productDTO.getTaxFreeYn() == null) productDTO.setTaxFreeYn("N");
        if (productDTO.getReceiptYn() == null) productDTO.setReceiptYn("Y");
        if (productDTO.getBizType() == null) productDTO.setBizType("1");
        if (productDTO.getRating() == null) productDTO.setRating(BigDecimal.ZERO);
        if (productDTO.getCreatedAt() == null) productDTO.setCreatedAt(LocalDateTime.now());
    }
}