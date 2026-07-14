package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dao.CategoryDAO;
import org.example.k_market.dto.CategoryDTO;
import org.example.k_market.dto.ProductDTO;
import org.example.k_market.entity.Product;
import org.example.k_market.entity.ProductOptionItems;
import org.example.k_market.entity.ProductOptions;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.security.MyUserDetails;
import org.example.k_market.service.ProductOptionService;
import org.example.k_market.service.ProductService;
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
    private final CategoryDAO categoryDAO;
    private final ShopRepository shopRepository;


    /**
     * 관리자 상품 목록
     *
     * 상품명, 상품번호, 판매자명으로 검색할 수 있다.
     */
    @GetMapping("/list")
    public String list(
            Model model,
            Authentication authentication,
            @RequestParam(value = "searchType", required = false)
            String searchType,
            @RequestParam(value = "keyword", required = false)
            String keyword,
            @RequestParam(value = "page", defaultValue = "0")
            int page) {

        List<ProductDTO> productList = isAdmin(authentication)
                ? productService.findAll()
                : currentShopNo(authentication)
                    .map(productService::findByShopNo)
                    .orElse(List.of());

        // 검색어가 있을 때 선택한 검색조건으로 목록 필터링
        if (keyword != null
                && !keyword.trim().isEmpty()
                && searchType != null) {

            String trimmedKeyword =
                    keyword.trim().toLowerCase();

            productList = productList.stream()
                    .filter(product -> switch (searchType) {

                        case "name" ->
                                product.getName() != null
                                        && product.getName()
                                        .toLowerCase()
                                        .contains(trimmedKeyword);

                        case "prodNo" ->
                                product.getProdNo() != null
                                        && String.valueOf(
                                        product.getProdNo()
                                ).contains(trimmedKeyword);

                        case "manufacturer" ->
                                product.getManufacturer() != null
                                        && product.getManufacturer()
                                        .toLowerCase()
                                        .contains(trimmedKeyword);

                        default -> true;
                    })
                    .toList();
        }

        model.addAttribute("productList", productList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "admin/product/list";
    }


    /**
     * 상품 등록 화면
     *
     * 카테고리, 상점정보와 빈 상품 DTO를 화면에 전달한다.
     */
    @GetMapping("/register")
    public String register(
            Model model,
            Authentication authentication) {

        List<CategoryDTO> allCategories =
                categoryDAO.findAll();

        Optional<Shop> currentShop =
                findCurrentUserShop(authentication);

        // 등록 실패 후 redirect 된 경우 기존 입력값 유지
        if (!model.containsAttribute("product")) {

            ProductDTO product = new ProductDTO();

            // 판매자 계정은 자신과 연결된 상점을 자동 설정
            if (!isAdmin(authentication)) {
                currentShop.map(Shop::getShopNo)
                        .ifPresent(product::setShopNo);
            }

            model.addAttribute("product", product);
        }

        model.addAttribute(
                "allCategories",
                allCategories
        );

        model.addAttribute(
                "shops",
                shopRepository.findAll()
        );

        model.addAttribute(
                "adminUser",
                isAdmin(authentication)
        );

        model.addAttribute(
                "currentShop",
                currentShop.orElse(null)
        );

        return "admin/product/register";
    }


    /**
     * 상품 등록 처리
     *
     * 1. 상품 기본정보와 이미지를 저장한다.
     * 2. 저장된 상품번호를 이용하여 옵션을 저장한다.
     */
    @PostMapping("/register")
    public String registerSubmit(
            @ModelAttribute("product")
            ProductDTO productDTO,

            @RequestParam(value = "file1", required = false)
            MultipartFile file1,

            @RequestParam(value = "file2", required = false)
            MultipartFile file2,

            @RequestParam(value = "file3", required = false)
            MultipartFile file3,

            @RequestParam(value = "detailFile", required = false)
            MultipartFile detailFile,

            @RequestParam(
                    value = "optionName[]",
                    required = false
            )
            List<String> optionNames,

            @RequestParam(
                    value = "optionValue[]",
                    required = false
            )
            List<String> optionValues,

            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // 필수 입력값과 상점 권한 검증
            validateProduct(
                    productDTO,
                    authentication
            );

            // 비어 있는 숫자·상태값 기본 설정
            applyDefaults(productDTO);

            // 상품 저장 후 자동 생성된 상품번호를 받는다.
            Product savedProduct =
                    productService.register(
                            productDTO,
                            file1,
                            file2,
                            file3,
                            detailFile
                    );

            // 등록 화면에서 입력한 옵션 저장
            productOptionService.saveOptions(
                    savedProduct.getProdNo(),
                    optionNames,
                    optionValues
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "상품이 등록되었습니다."
            );

            return "redirect:/admin/product/list";

        } catch (IllegalArgumentException | IOException e) {

            log.error("상품 등록 실패", e);

            redirectAttributes.addFlashAttribute(
                    "productError",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "product",
                    productDTO
            );

            return "redirect:/admin/product/register";
        }
    }


    /**
     * 상품 수정 화면
     *
     * 상품 기본정보뿐 아니라 카테고리, 상점,
     * 기존 옵션과 옵션항목까지 함께 조회한다.
     */
    @GetMapping("/modify/{prodNo}")
    public String modifyForm(
            @PathVariable("prodNo")
            Long prodNo,
            Model model,
            Authentication authentication) {

        Product product =
                productService.findById(prodNo);

        if (product == null) {
            return "redirect:/admin/product/list";
        }
        assertProductOwnership(product, authentication);

        // 해당 상품에 등록된 옵션 그룹 조회
        List<ProductOptions> options =
                productOptionService
                        .findOptionsByProdNo(prodNo);

        // 옵션 그룹별 옵션항목 조회
        Map<Long, List<ProductOptionItems>>
                optionItemsMap =
                productOptionService
                        .findOptionItemsMap(options);

        /*
         * 수정 화면의 옵션항목 input에 표시하기 위해
         * 항목 이름을 쉼표로 연결한다.
         *
         * 예:
         * 사이즈 → S, M, L, XL
         */
        Map<Long, String> optionValueMap =
                createOptionValueMap(
                        options,
                        optionItemsMap
                );

        Optional<Shop> currentShop =
                findCurrentUserShop(authentication);

        model.addAttribute(
                "product",
                product.toDTO()
        );

        model.addAttribute(
                "allCategories",
                categoryDAO.findAll()
        );

        model.addAttribute(
                "shops",
                shopRepository.findAll()
        );

        model.addAttribute(
                "adminUser",
                isAdmin(authentication)
        );

        model.addAttribute(
                "currentShop",
                currentShop.orElse(null)
        );

        model.addAttribute(
                "options",
                options
        );

        model.addAttribute(
                "optionItemsMap",
                optionItemsMap
        );

        model.addAttribute(
                "optionValueMap",
                optionValueMap
        );

        return "admin/product/modify";
    }


    /**
     * 상품 수정 처리
     *
     * 상품 기본정보와 이미지를 수정한 후
     * 기존 옵션을 새로 입력한 옵션으로 교체한다.
     */
    @PostMapping("/modify")
    public String modifySubmit(
            @ModelAttribute("product")
            ProductDTO productDTO,

            BindingResult bindingResult,

            @RequestParam(value = "file1", required = false)
            MultipartFile file1,

            @RequestParam(value = "file2", required = false)
            MultipartFile file2,

            @RequestParam(value = "file3", required = false)
            MultipartFile file3,

            @RequestParam(value = "detailFile", required = false)
            MultipartFile detailFile,

            @RequestParam(
                    value = "optionName[]",
                    required = false
            )
            List<String> optionNames,

            @RequestParam(
                    value = "optionValue[]",
                    required = false
            )
            List<String> optionValues,

            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            log.error("상품 수정 바인딩 오류");

            bindingResult.getAllErrors()
                    .forEach(error ->
                            log.error(error.toString())
                    );

            redirectAttributes.addFlashAttribute(
                    "productError",
                    "입력값을 다시 확인해 주세요."
            );

            return "redirect:/admin/product/modify/"
                    + productDTO.getProdNo();
        }

        try {
            Product existingProduct = productService.findById(productDTO.getProdNo());
            if (existingProduct == null) throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
            assertProductOwnership(existingProduct, authentication);
            // 수정 시에도 카테고리와 상점정보 검증
            validateProduct(
                    productDTO,
                    authentication
            );

            applyDefaults(productDTO);

            // 상품 기본정보와 이미지 수정
            Product modifiedProduct =
                    productService.modify(
                            productDTO,
                            file1,
                            file2,
                            file3,
                            detailFile
                    );

            /*
             * 기존 옵션과 옵션항목을 삭제한 뒤
             * 수정 화면에서 전달된 옵션으로 다시 저장한다.
             */
            productOptionService.replaceOptions(
                    modifiedProduct.getProdNo(),
                    optionNames,
                    optionValues
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "상품이 수정되었습니다."
            );

            return "redirect:/admin/product/list";

        } catch (IllegalArgumentException | IOException e) {

            log.error(
                    "상품 수정 실패 - prodNo: {}",
                    productDTO.getProdNo(),
                    e
            );

            redirectAttributes.addFlashAttribute(
                    "productError",
                    e.getMessage()
            );

            return "redirect:/admin/product/modify/"
                    + productDTO.getProdNo();
        }
    }


    /**
     * 상품 한 건 삭제
     */
    @PostMapping("/delete/{prodNo}")
    @ResponseBody
    public Map<String, Object> deleteProduct(
            @PathVariable("prodNo")
            Long prodNo,
            Authentication authentication) {

        try {
            Product product = productService.findById(prodNo);
            if (product == null) throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
            assertProductOwnership(product, authentication);
            /*
             * 상품 삭제 전에 옵션과 옵션항목을 먼저 삭제한다.
             * 외래키가 연결되어 있을 경우 오류를 방지할 수 있다.
             */
            productOptionService
                    .deleteOptionsByProdNo(prodNo);

            productService.delete(prodNo);

            return Map.of(
                    "success", true,
                    "message", "상품이 삭제되었습니다."
            );

        } catch (Exception e) {

            log.error(
                    "상품 삭제 실패 - prodNo: {}",
                    prodNo,
                    e
            );

            return Map.of(
                    "success", false,
                    "message", "상품 삭제에 실패했습니다."
            );
        }
    }


    /**
     * 선택한 상품 여러 건 삭제
     */
    @PostMapping("/deleteSelected")
    @ResponseBody
    public Map<String, Object> deleteSelectedProducts(
            @RequestBody
            List<Long> prodNos,
            Authentication authentication) {

        try {
            if (prodNos == null || prodNos.isEmpty()) {
                throw new IllegalArgumentException(
                        "삭제할 상품을 선택해 주세요."
                );
            }

            for (Long prodNo : prodNos) {
                Product product = productService.findById(prodNo);
                if (product == null) throw new IllegalArgumentException("상품을 찾을 수 없습니다.");
                assertProductOwnership(product, authentication);
            }

            // 각 상품의 옵션을 먼저 삭제
            for (Long prodNo : prodNos) {
                productOptionService
                        .deleteOptionsByProdNo(prodNo);
            }

            productService.deleteAll(prodNos);

            return Map.of(
                    "success", true,
                    "message", "선택한 상품이 삭제되었습니다."
            );

        } catch (Exception e) {

            log.error("상품 일괄 삭제 실패", e);

            return Map.of(
                    "success", false,
                    "message", "상품 일괄 삭제에 실패했습니다."
            );
        }
    }


    /**
     * 수정 화면용 옵션항목 문자열 생성
     *
     * 여러 옵션항목을 쉼표로 연결하여
     * 하나의 input에 표시할 수 있도록 변환한다.
     */
    private Map<Long, String> createOptionValueMap(
            List<ProductOptions> options,
            Map<Long, List<ProductOptionItems>>
                    optionItemsMap) {

        Map<Long, String> optionValueMap =
                new LinkedHashMap<>();

        if (options == null || options.isEmpty()) {
            return optionValueMap;
        }

        for (ProductOptions option : options) {

            List<ProductOptionItems> items =
                    optionItemsMap.getOrDefault(
                            option.getOptionNo(),
                            List.of()
                    );

            String optionValue =
                    items.stream()
                            .map(
                                    ProductOptionItems
                                            ::getItemName
                            )
                            .filter(name ->
                                    name != null
                                            && !name.isBlank()
                            )
                            .collect(
                                    Collectors.joining(", ")
                            );

            optionValueMap.put(
                    option.getOptionNo(),
                    optionValue
            );
        }

        return optionValueMap;
    }


    /**
     * 상품 입력값 검증
     */
    private void validateProduct(
            ProductDTO productDTO,
            Authentication authentication) {

        if (productDTO.getCateNo() == null) {
            throw new IllegalArgumentException(
                    "상품 카테고리를 선택해 주세요."
            );
        }

        if (categoryDAO
                .findById(productDTO.getCateNo())
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "선택한 카테고리가 존재하지 않습니다."
            );
        }

        if (productDTO.getName() == null
                || productDTO.getName()
                .trim()
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "상품명을 입력해 주세요."
            );
        }

        if (productDTO.getPrice() == null) {
            throw new IllegalArgumentException(
                    "상품 가격을 입력해 주세요."
            );
        }

        /*
         * 관리자는 선택한 상점을 사용하고,
         * 일반 판매자는 로그인 계정과 연결된 상점을 사용한다.
         */
        productDTO.setShopNo(
                resolveShopNo(
                        productDTO.getShopNo(),
                        authentication
                )
        );
    }


    /**
     * 로그인 권한에 따라 상품에 연결할 상점번호 결정
     */
    private Integer resolveShopNo(
            Integer requestedShopNo,
            Authentication authentication) {

        if (isAdmin(authentication)) {

            if (requestedShopNo == null) {
                throw new IllegalArgumentException(
                        "상점을 선택해 주세요."
                );
            }

            return shopRepository
                    .findByShopNo(requestedShopNo)
                    .map(Shop::getShopNo)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "선택한 상점이 존재하지 않습니다."
                            )
                    );
        }

        Shop shop =
                findCurrentUserShop(authentication)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "로그인한 판매자와 연결된 상점이 없습니다."
                                )
                        );

        if (shop.getShopNo() == null) {
            throw new IllegalArgumentException(
                    "연결된 상점 번호가 생성되지 않았습니다."
            );
        }

        return shop.getShopNo();
    }


    /**
     * 로그인한 판매자와 연결된 상점 조회
     */
    private Optional<Shop> findCurrentUserShop(
            Authentication authentication) {

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof MyUserDetails userDetails)) {

            return Optional.empty();
        }

        return shopRepository.findById(
                userDetails.getUser().getMemberNo()
        );
    }

    private Optional<Integer> currentShopNo(Authentication authentication) {
        return findCurrentUserShop(authentication).map(Shop::getShopNo);
    }

    private void assertProductOwnership(Product product, Authentication authentication) {
        if (isAdmin(authentication)) return;
        Integer shopNo = currentShopNo(authentication)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                        "판매자 상점 정보를 찾을 수 없습니다."));
        if (!shopNo.equals(product.getShopNo())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "다른 판매자의 상품에는 접근할 수 없습니다.");
        }
    }


    /**
     * 현재 로그인 사용자의 관리자 권한 확인
     */
    private boolean isAdmin(
            Authentication authentication) {

        return authentication != null
                && authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(
                                authority.getAuthority()
                        )
                );
    }


    /**
     * 비어 있는 상품값에 기본값 설정
     */
    private void applyDefaults(
            ProductDTO productDTO) {

        productDTO.setName(
                productDTO.getName().trim()
        );

        if (productDTO.getDiscountRate() == null) {
            productDTO.setDiscountRate(0);
        }

        if (productDTO.getRewardPoints() == null) {
            productDTO.setRewardPoints(0);
        }

        if (productDTO.getStockQuantity() == null) {
            productDTO.setStockQuantity(0);
        }

        if (productDTO.getShippingFee() == null) {
            productDTO.setShippingFee(0);
        }

        if (productDTO.getViewCount() == null) {
            productDTO.setViewCount(0);
        }

        if (productDTO.getSalesCount() == null) {
            productDTO.setSalesCount(0);
        }

        if (productDTO.getStatus() == null) {
            productDTO.setStatus("ACTIVE");
        }

        if (productDTO.getTaxFreeYn() == null) {
            productDTO.setTaxFreeYn("N");
        }

        if (productDTO.getReceiptYn() == null) {
            productDTO.setReceiptYn("Y");
        }

        if (productDTO.getBizType() == null) {
            productDTO.setBizType("1");
        }

        if (productDTO.getRating() == null) {
            productDTO.setRating(BigDecimal.ZERO);
        }

        if (productDTO.getCreatedAt() == null) {
            productDTO.setCreatedAt(
                    LocalDateTime.now()
            );
        }
    }
}
