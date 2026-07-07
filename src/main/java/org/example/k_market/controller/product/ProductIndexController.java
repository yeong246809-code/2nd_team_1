package org.example.k_market.controller.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
// 1. 엔티티와 레포지토리 패키지를 임포트하세요 (본인 프로젝트 경로에 맞추기)
import org.example.k_market.entity.Product;
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

    // 2. DBeaver 데이터를 긁어올 Repository 의존성 주입
    private final ProductRepository productRepository;

    @GetMapping("/product/list")
    public String list() {
        return "product/list";
    }

    @GetMapping("/product/search")
    public String search(@RequestParam(value = "keyword", defaultValue = "셔츠") String keyword, Model model) {

        // 3. 임시 가짜 Map 데이터 대신, DBeaver 실데이터(Entity)를 긁어옵니다.
        List<Product> products = productRepository.findAll();

        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", products.size());
        model.addAttribute("products", products); // HTML의 th:each="product : ${products}"로 직행합니다.

        return "product/search";
    }

    @GetMapping("/product/view")
    public String view(Model model) {
        model.addAttribute("product", sampleProduct());
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