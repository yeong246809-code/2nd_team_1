package org.example.k_market.controller.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.k_market.dao.CategoryDAO;
import org.example.k_market.dao.ProductDAO;
import org.example.k_market.dto.CategoryDTO;
import org.example.k_market.dto.ProductDTO;
import org.example.k_market.entity.Shop;
import org.example.k_market.repository.ShopRepository;
import org.example.k_market.security.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
    private final CategoryDAO categoryDAO;
    private final ShopRepository shopRepository;
    private final UsersRepository usersRepository;

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

        // 이 로그를 확인해서 카테고리가 0개가 아니어야 합니다.
        log.info("조회된 카테고리 수: " + allCategories.size());

        model.addAttribute("product", new ProductDTO());
        model.addAttribute("allCategories", allCategories);
        model.addAttribute("shops", shopRepository.findAll());
        model.addAttribute("adminUser", isAdmin(authentication));
        model.addAttribute("currentShop", currentShop.orElse(null));
        return "admin/product/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("product") ProductDTO productDTO,
                                 @RequestParam("file1") MultipartFile file1,
                                 @RequestParam("file2") MultipartFile file2,
                                 @RequestParam("file3") MultipartFile file3,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) throws IOException {
        try {
            validateProduct(productDTO, authentication);
            applyDefaults(productDTO);

            String uploadPath = new File("uploads").getAbsolutePath();
            productDTO.setThumb1(saveFile(file1, uploadPath));
            productDTO.setThumb2(saveFile(file2, uploadPath));
            productDTO.setThumb3(saveFile(file3, uploadPath));

            productDAO.save(productDTO);
            return "redirect:/admin/product/list";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("productError", e.getMessage());
            redirectAttributes.addFlashAttribute("product", productDTO);
            return "redirect:/admin/product/register";
        }
    }

    private void validateProduct(ProductDTO productDTO, Authentication authentication) {
        if (productDTO.getCateNo() == null) {
            throw new IllegalArgumentException("상품 카테고리를 선택해주세요.");
        }
        if (categoryDAO.findById(productDTO.getCateNo()).isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("상품명을 입력해주세요.");
        }
        if (productDTO.getPrice() == null) {
            throw new IllegalArgumentException("판매가격을 입력해주세요.");
        }

        productDTO.setShopNo(resolveShopNo(productDTO.getShopNo(), authentication));
    }

    private Integer resolveShopNo(Integer requestedShopNo, Authentication authentication) {
        if (isAdmin(authentication)) {
            if (requestedShopNo == null) {
                throw new IllegalArgumentException("상품을 등록할 상점을 선택해주세요.");
            }
            return shopRepository.findByShopNo(requestedShopNo)
                    .map(Shop::getShopNo)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상점입니다."));
        }

        Shop shop = findCurrentUserShop(authentication)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 판매자와 연결된 상점이 없습니다."));
        if (shop.getShopNo() == null) {
            throw new IllegalArgumentException("상점 번호가 아직 생성되지 않았습니다.");
        }
        return shop.getShopNo();
    }

    private Optional<Shop> findCurrentUserShop(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof MyUserDetails userDetails)) {
            return Optional.empty();
        }
        return shopRepository.findById(userDetails.getUser().getMemberNo());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private void applyDefaults(ProductDTO productDTO) {
        productDTO.setName(productDTO.getName().trim());
        if (productDTO.getDiscountRate() == null) productDTO.setDiscountRate(0);
        if (productDTO.getRewardPoints() == null) productDTO.setRewardPoints(0);
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

    private String saveFile(MultipartFile file, String uploadPath) throws IOException {
        if (file == null || file.isEmpty()) return null;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("Failed to create upload directory: " + uploadDir.getAbsolutePath());
        }

        String originalName = file.getOriginalFilename() == null ? "upload" : new File(file.getOriginalFilename()).getName();
        String fileName = UUID.randomUUID() + "_" + originalName;
        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);
        return fileName;
    }

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

    @PostMapping("/modify")
    public String modifySubmit(@ModelAttribute ProductDTO productDTO, org.springframework.validation.BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("상품 수정 바인딩 에러 발생");
            bindingResult.getAllErrors().forEach(error -> log.error(error.toString()));
            return "admin/product/modify";
        }

        log.info("상품 수정 실행 - prodNo: {}", productDTO.getProdNo());

        if (productDTO.getCreatedAt() == null) {
            productDTO.setCreatedAt(LocalDateTime.now());
        }

        productDAO.save(productDTO);
        return "redirect:/admin/product/list";
    }

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
