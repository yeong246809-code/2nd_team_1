package org.example.k_market.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dao.CategoryDAO;
import org.example.k_market.dao.ProductDAO;
import org.example.k_market.dto.CategoryDTO;
import org.example.k_market.dto.ProductDTO;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class AdminProductController {

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final UsersRepository usersRepository;

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
        List<CategoryDTO> allCategories = categoryDAO.findAll();

        // 이 로그를 확인해서 카테고리가 0개가 아니어야 합니다.
        log.info("조회된 카테고리 수: " + allCategories.size());

        model.addAttribute("product", new ProductDTO());
        model.addAttribute("allCategories", allCategories);
        return "admin/product/register";
    }

    // AdminProductController.java
    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("product") ProductDTO productDTO,
                                 @RequestParam("file1") MultipartFile file1,
                                 @RequestParam("file2") MultipartFile file2,
                                 @RequestParam("file3") MultipartFile file3,
                                 HttpSession session) throws IOException { // 💡 HttpSession 추가

        // 1. 로그인한 유저의 정보를 세션(sessUser)에서 가져오기
        String userId = (String) session.getAttribute("sessUser");
        if (userId == null) {
            log.error("로그인 세션이 만료되었습니다.");
            return "redirect:/member/login";
        }

        // 2. 유저 정보를 조회하여 shopNo를 자동으로 세팅
        // (Users 엔티티에서 shopNo를 가져옵니다)
        org.example.k_market.entity.Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        productDTO.setShopNo(user.getMemberNo());

        // 3. 파일 저장 로직
        String uploadPath = new File("uploads/").getAbsolutePath() + File.separator;
        productDTO.setThumb1(saveFile(file1, uploadPath));
        productDTO.setThumb2(saveFile(file2, uploadPath));
        productDTO.setThumb3(saveFile(file3, uploadPath));

        // 4. 필수 기본값 설정
        productDTO.setCreatedAt(LocalDateTime.now());
        if (productDTO.getBizType() == null) productDTO.setBizType("1");
        if (productDTO.getRating() == null) productDTO.setRating(java.math.BigDecimal.ZERO);

        // 5. 저장
        productDAO.save(productDTO);
        return "redirect:/admin/product/list";
    }

    // 파일 저장 로직 분리
    private String saveFile(MultipartFile file, String uploadPath) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File dest = new File(uploadPath + fileName);
        file.transferTo(dest);
        return fileName; // DB에는 파일명만 저장
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