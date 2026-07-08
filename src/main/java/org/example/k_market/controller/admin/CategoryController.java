package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.CategoryNodeDTO;
import org.example.k_market.service.admin.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
// ★ 클래스 경로를 명확하게 변경
@RequestMapping("/admin/config/category")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 관리 페이지 렌더링
    @GetMapping
    public String categoryPage(Model model) {
        List<CategoryNodeDTO> categoryTree = categoryService.getCategoryTree();
        model.addAttribute("categoryList", categoryTree);
        return "admin/config/category";
    }

    // ★ POST 경로 매핑 (확실하게 /saveAll 로 지정)
    @PostMapping("/saveAll")
    @ResponseBody
    public ResponseEntity<String> saveAllCategories(@RequestBody List<CategoryNodeDTO> categories) {
        // ★ 백엔드 로그 1: 컨트롤러에 무사히 들어왔는지 확인
        System.out.println("=========================================");
        System.out.println("🚀 [CategoryController] /saveAll 진입 성공!");
        System.out.println("📦 전송받은 카테고리 데이터: " + categories);
        System.out.println("=========================================");

        try {
            categoryService.saveAllCategories(categories);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("error");
        }
    }
}