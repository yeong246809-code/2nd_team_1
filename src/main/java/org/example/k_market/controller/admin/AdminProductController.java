package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dao.ProductDAO;
import org.example.k_market.dto.ProductDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {

    private final ProductDAO productDAO;

    /**
     * [목록 & 검색]
     */
    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "searchType", required = false) String searchType,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "page", defaultValue = "0") int page) {

        List<ProductDTO> productList = productDAO.findAll();

        if (keyword != null && !keyword.trim().isEmpty() && searchType != null) {
            String trimmedKeyword = keyword.trim().toLowerCase();
            productList = productList.stream()
                    .filter(product -> {
                        switch (searchType) {
                            case "name":
                                return product.getName() != null && product.getName().toLowerCase().contains(trimmedKeyword);
                            case "prodNo":
                                return product.getProdNo() != null && String.valueOf(product.getProdNo()).contains(trimmedKeyword);
                            case "manufacturer":
                                return product.getManufacturer() != null && product.getManufacturer().toLowerCase().contains(trimmedKeyword);
                            default:
                                return true;
                        }
                    }).toList();
        }

        model.addAttribute("productList", productList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 1);
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "admin/product/list";
    }

    /**
     * [등록 폼 이동]
     */
    @GetMapping("/register")
    public String register(Model model) {
        // 이 한 줄이 없으면 Thymeleaf는 'product'가 뭔지 몰라 에러를 냅니다.
        model.addAttribute("product", new ProductDTO());
        return "admin/product/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute ProductDTO productDTO) {
        // 1. 실제 DB에 존재하는 카테고리 번호를 가져와 강제 할당 (외래키 제약조건 회피)
        // 기존에 존재하는 상품 중 하나를 샘플로 가져와 그 카테고리 정보를 사용
        List<ProductDTO> allProducts = productDAO.findAll();
        if (!allProducts.isEmpty()) {
            ProductDTO sample = allProducts.get(0);
            productDTO.setCateNo(sample.getCateNo());
            productDTO.setShopNo(sample.getShopNo());
        } else {
            // 상품이 하나도 없을 경우를 대비한 기본값
            productDTO.setCateNo(1010);
            productDTO.setShopNo(1);
        }

        // 2. 누락될 수 있는 필수 필드 초기화 (null 방지)
        productDTO.setCreatedAt(LocalDateTime.now());
        productDTO.setBizType("1");
        productDTO.setRating(java.math.BigDecimal.ZERO);

        productDAO.save(productDTO);
        return "redirect:/admin/product/list";
    }

    /**
     * [수정 폼 이동]
     */
    @GetMapping("/modify/{prodNo}")
    public String modifyForm(@PathVariable("prodNo") Long prodNo, Model model) {
        log.info("상품 수정 페이지 요청 - prodNo: {}", prodNo);
        Optional<ProductDTO> productOpt = productDAO.findById(prodNo);

        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "admin/product/modify";
        }
        return "redirect:/admin/product/list";
    }

    /**
     * [수정 처리] - 에러가 나더라도 콘솔에 원인이 찍히도록 BindingResult 추가
     */
    @PostMapping("/modify")
    public String modifySubmit(@ModelAttribute ProductDTO productDTO, org.springframework.validation.BindingResult bindingResult) {

        // 만약 타입 바인딩 오류가 발생했다면 콘솔에 에러 로그를 정확히 출력합니다.
        if (bindingResult.hasErrors()) {
            log.error("====== 상품 수정 바인딩 에러 발생 ======");
            bindingResult.getAllErrors().forEach(error -> log.error(error.toString()));
            return "admin/product/modify"; // 에러 발생 시 수정 페이지에 그대로 머무름
        }

        log.info("상품 수정 실행 - prodNo: {}", productDTO.getProdNo());

        // 안전하게 영속성 저장을 위해 기존 생성일이 유실되었다면 현재 시간으로 방어 코드 작성
        if (productDTO.getCreatedAt() == null) {
            productDTO.setCreatedAt(LocalDateTime.now());
        }

        productDAO.save(productDTO);
        return "redirect:/admin/product/list";
    }

    /**
     * [단일 삭제]
     */
    @PostMapping("/delete/{prodNo}")
    @ResponseBody
    public Map<String, Object> deleteProduct(@PathVariable("prodNo") Long prodNo) {
        try {
            productDAO.deleteById(prodNo);
            return Map.of("success", true, "message", "상품이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "삭제 실패");
        }
    }

    /**
     * [일괄 삭제]
     */
    @PostMapping("/deleteSelected")
    @ResponseBody
    public Map<String, Object> deleteSelectedProducts(@RequestBody List<Long> prodNos) {
        try {
            for (Long prodNo : prodNos) {
                productDAO.deleteById(prodNo);
            }
            return Map.of("success", true, "message", "선택 상품 일괄 삭제 완료");
        } catch (Exception e) {
            return Map.of("success", false, "message", "일괄 삭제 실패");
        }
    }
}