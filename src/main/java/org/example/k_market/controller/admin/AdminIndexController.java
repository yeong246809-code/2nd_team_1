package org.example.k_market.controller.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.QnaDTO;
import org.example.k_market.entity.Notice;
import org.example.k_market.entity.Qna;
import org.example.k_market.entity.Users;
import org.example.k_market.repository.UsersRepository;
import org.example.k_market.service.admin.OrderService;
import org.example.k_market.service.cs.NoticeService;
import org.example.k_market.service.cs.QnaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminIndexController {

    private final NoticeService noticeService;
    private final QnaService qnaService;
    private final UsersRepository usersRepository;
    private final OrderService orderService;

    @GetMapping({ "/index"})
    public String index(Model model) {
        // [1] 운영현황 & 주요지표 (오늘/어제) 바인딩
        model.addAttribute("todayStats", orderService.getTodayDashboardStats());
        model.addAttribute("yesterdayStats", orderService.getYesterdayDashboardStats());

        // [2] 5일 차트 통계 바인딩
        model.addAttribute("dailySummaryList", orderService.getRecent5DaysSummary());
        model.addAttribute("topSalesList", orderService.getTopSalesCategories(5));

        // 1. 최신 공지사항 5개 조회
        List<Notice> noticeList = noticeService.findTop5();

        // 2. 최신 고객문의 5개 조회 (원글 기준)
        List<Qna> qnaList = qnaService.findTop5();

        // 3. Qna 엔티티를 DTO로 변환하면서 작성자 아이디(id) 매핑
        List<QnaDTO> qnaDtoList = qnaList.stream().map(qna -> {
            Users user = usersRepository.findByMemberNo(qna.getMemberNo()).orElse(null);

            return QnaDTO.builder()
                    .no(qna.getNo())
                    .type1(qna.getType1())
                    .type2(qna.getType2())
                    .title(qna.getTitle())
                    .content(qna.getContent())
                    .memberNo(qna.getMemberNo())
                    .id(user != null ? user.getId() : "탈퇴회원") // 아이디 매핑
                    .isAnswered(qna.getIsAnswered())
                    .createdAt(qna.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());

        // 4. 모델에 담아서 HTML로 전달
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("qnaList", qnaDtoList);
        model.addAttribute("dailySummaryList", orderService.getRecent5DaysSummary());
        model.addAttribute("topSalesList", orderService.getTopSalesCategories(5));

        return "admin/index";
    }
}