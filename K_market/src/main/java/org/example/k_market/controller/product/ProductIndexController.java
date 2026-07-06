package org.example.k_market.controller.product;

import lombok.extern.log4j.Log4j2;
import org.example.k_market.dto.ProductDTO; // 본인의 DTO 패키지 경로
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Controller
@RequestMapping("/product")
public class ProductIndexController {

    // 💡 디비버에서 본 데이터를 임시로 저장해둘 리스트
    private static final List<ProductDTO> mockProducts = new ArrayList<>();

    // 클래스가 로딩될 때 디비버 내부 데이터와 똑같이 세팅 (테스트용)
    // 클래스가 로딩될 때 디비버 내부 데이터와 똑같이 세팅 (테스트용)
    static {
        mockProducts.add(ProductDTO.builder()
                .prodNo(1L)
                .cateNo(3)
                .shopNo(2L)
                .name("꿀사과 1박스")
                .description("맛있는 사과")
                .manufacturer("신선농장")
                .price(30000)
                .build());

        mockProducts.add(ProductDTO.builder()
                .prodNo(2L)
                .cateNo(2)
                .shopNo(3L)
                .name("여름 반팔티")
                .description("시원한 반팔")
                .manufacturer("멋쟁이옷장")
                .price(15000)
                .build());

        mockProducts.add(ProductDTO.builder()
                .prodNo(3L)
                .cateNo(1)
                .shopNo(1L)
                .name("다목적 세정제")
                .description("깨끗하게 닦여요")
                .manufacturer("다이쏘")
                .price(5000)
                .build());

        mockProducts.add(ProductDTO.builder()
                .prodNo(4L)
                .cateNo(3)
                .shopNo(2L)
                .name("꿀사과 1박스")
                .description("맛있는 사과")
                .manufacturer("신선농장")
                .price(30000)
                .build());

        mockProducts.add(ProductDTO.builder()
                .prodNo(5L)
                .cateNo(2)
                .shopNo(3L)
                .name("여름 반팔티")
                .description("시원한 반팔")
                .manufacturer("멋쟁이옷장")
                .price(15000)
                .build());

        mockProducts.add(ProductDTO.builder()
                .prodNo(6L)
                .cateNo(1)
                .shopNo(1L)
                .name("다목적 세정제")
                .description("깨끗하게 닦여요")
                .manufacturer("다이쏘")
                .price(5000)
                .build());
    }

    // 1. 장바구니 페이지 이동 (/product/cart)
    @GetMapping("/cart")
    public String cart() {
        log.info("ProductController - 장바구니 페이지 이동");
        return "product/cart";
    }

    // 2. 상품 목록 페이지 (/product/list)
    @GetMapping("/list")
    public String list(Model model) {
        log.info("ProductController - 임시 상품 목록 데이터 전달");

        // 서비스 대신 임시 리스트를 통째로 타임리프에 넘김
        model.addAttribute("products", mockProducts);
        return "product/list";
    }

    // 3. 상품 상세 보기 페이지 (/product/view)
    // 주소창 예시: http://localhost:8080/product/view?prodNo=1
    @GetMapping("/view")
    public String view(@RequestParam(value = "prodNo", defaultValue = "1") int prodNo, Model model) {
        log.info("ProductController - 임시 상품 상세 데이터 전달 (번호: " + prodNo + ")");

        // 입력받은 prodNo에 해당하는 상품 찾기 (없으면 0번째 바나나/사과 출력)
        ProductDTO targetProduct = mockProducts.stream()
                .filter(p -> p.getProdNo() == prodNo)
                .findFirst()
                .orElse(mockProducts.get(0));

        model.addAttribute("product", targetProduct);
        return "product/view";
    }
}