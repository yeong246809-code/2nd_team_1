package org.example.k_market.controller.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
// 1. 엔티티와 레포지토리 패키지를 임포트하세요 (본인 프로젝트 경로에 맞추기)
import org.example.k_market.entity.Category;
import org.example.k_market.entity.Product;
import org.example.k_market.repository.CategoryRepository;
import org.example.k_market.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor // Repository 자동 주입을 위해 필수!
public class ProductIndexController {

    private final CategoryRepository categoryRepository; // 이미 있을 가능성 높음
    private final ProductRepository productRepository;   // 탭에 열려있던 그거

    @GetMapping("/product/list")
    public String list(@RequestParam(required = false) Integer cateNo, Model model) {
        List<Category> categories = categoryRepository.findByParentNoIsNull(); // 대분류만
        List<Product> products = (cateNo == null)
                ? productRepository.findAll()
                : productRepository.findByCateNo(cateNo);

        model.addAttribute("categories", categories);
        model.addAttribute("products", products);
        return "product/list";
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

        return "product/search";
    }

    @GetMapping("/product/view")
    public String view(@RequestParam Integer prodNo, Model model) {
        Product product = productRepository.findById(Long.valueOf(prodNo))
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다: " + prodNo));

        Category category = categoryRepository.findById(product.getCateNo()).orElse(null);
        Category parentCategory = (category != null && category.getParentNo() != null)
                ? categoryRepository.findById(category.getParentNo()).orElse(null)
                : null;

        // 사이드바 활성화 표시용 - 대분류 catNo
        Integer mainCateNo = (parentCategory != null) ? parentCategory.getCateNo() : (category != null ? category.getCateNo() : null);

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findByParentNoIsNull());
        model.addAttribute("parentCategory", parentCategory);
        model.addAttribute("mainCateNo", mainCateNo);
        return "product/view";
    }

    @GetMapping("/product/cart")
    public String cart() {
        return "product/cart";
    }

    @GetMapping("/product/order")
    public String order(Model model) {
        model.addAttribute("product", sampleProduct());
        model.addAttribute("order", Map.of("totalPrice", "24,650"));
        return "product/order";
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
}
