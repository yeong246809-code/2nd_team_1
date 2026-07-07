package org.example.k_market.controller.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@Log4j2
@RequiredArgsConstructor
public class ProductIndexController {

    @GetMapping("/product/list")
    public String list() {
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
