package org.example.k_market.controller.admin;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "searchType", required = false) String searchType,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "page", defaultValue = "0") int page) {
        List<ProductDTO> productList = productDAO.findAll();

        if (keyword != null && !keyword.trim().isEmpty() && searchType != null) {
            String trimmedKeyword = keyword.trim().toLowerCase();
            productList = productList.stream()
                    .filter(product -> switch (searchType) {
                        case "name" -> product.getName() != null
                                && product.getName().toLowerCase().contains(trimmedKeyword);
                        case "prodNo" -> product.getProdNo() != null
                                && String.valueOf(product.getProdNo()).contains(trimmedKeyword);
                        case "manufacturer" -> product.getManufacturer() != null
                                && product.getManufacturer().toLowerCase().contains(trimmedKeyword);
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
            saveProductImages(productDTO, file1, file2, file3);
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
            throw new IllegalArgumentException("Please select a product category.");
        }
        if (categoryDAO.findById(productDTO.getCateNo()).isEmpty()) {
            throw new IllegalArgumentException("The selected category does not exist.");
        }
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a product name.");
        }
        if (productDTO.getPrice() == null) {
            throw new IllegalArgumentException("Please enter a product price.");
        }

        productDTO.setShopNo(resolveShopNo(productDTO.getShopNo(), authentication));
    }

    private Integer resolveShopNo(Integer requestedShopNo, Authentication authentication) {
        if (isAdmin(authentication)) {
            if (requestedShopNo == null) {
                throw new IllegalArgumentException("Please select a shop for this product.");
            }
            return shopRepository.findByShopNo(requestedShopNo)
                    .map(Shop::getShopNo)
                    .orElseThrow(() -> new IllegalArgumentException("The selected shop does not exist."));
        }

        Shop shop = findCurrentUserShop(authentication)
                .orElseThrow(() -> new IllegalArgumentException("No shop is linked to the logged-in seller."));
        if (shop.getShopNo() == null) {
            throw new IllegalArgumentException("The linked shop number has not been created yet.");
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
    }

    private void saveProductImages(ProductDTO productDTO,
                                   MultipartFile file1,
                                   MultipartFile file2,
                                   MultipartFile file3) throws IOException {
        String uploadPath = new File("uploads").getAbsolutePath();
        productDTO.setThumb1(saveFile(file1, uploadPath));
        productDTO.setThumb2(saveFile(file2, uploadPath));
        productDTO.setThumb3(saveFile(file3, uploadPath));
    }

    private String saveFile(MultipartFile file, String uploadPath) throws IOException {
        if (file == null || file.isEmpty()) return null;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("Failed to create upload directory: " + uploadDir.getAbsolutePath());
        }

        String originalName = file.getOriginalFilename() == null
                ? "upload"
                : new File(file.getOriginalFilename()).getName();
        String fileName = UUID.randomUUID() + "_" + originalName;
        File dest = new File(uploadDir, fileName);
        file.transferTo(dest);
        return fileName;
    }

    @GetMapping("/modify/{prodNo}")
    public String modifyForm(@PathVariable("prodNo") Long prodNo, Model model) {
        log.info("Product modify page requested - prodNo: {}", prodNo);
        Optional<ProductDTO> productOpt = productDAO.findById(prodNo);

        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            return "admin/product/modify";
        }
        return "redirect:/admin/product/list";
    }

    @PostMapping("/modify")
    public String modifySubmit(@ModelAttribute ProductDTO productDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Product modify binding error");
            bindingResult.getAllErrors().forEach(error -> log.error(error.toString()));
            return "admin/product/modify";
        }

        log.info("Product modify submitted - prodNo: {}", productDTO.getProdNo());

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
            return Map.of("success", true, "message", "Product deleted successfully.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Delete failed.");
        }
    }

    @PostMapping("/deleteSelected")
    @ResponseBody
    public Map<String, Object> deleteSelectedProducts(@RequestBody List<Long> prodNos) {
        try {
            for (Long prodNo : prodNos) {
                productDAO.deleteById(prodNo);
            }
            return Map.of("success", true, "message", "Selected products deleted successfully.");
        } catch (Exception e) {
            return Map.of("success", false, "message", "Bulk delete failed.");
        }
    }
}
