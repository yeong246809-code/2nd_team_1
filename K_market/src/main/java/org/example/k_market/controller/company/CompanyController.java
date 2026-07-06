package org.example.k_market.controller.company;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dao.CmMediaDAO;   // 본인 패키지 경로에 맞게 매칭
import org.example.k_market.dao.CmRecruitDAO; // 본인 패키지 경로에 맞게 매칭
import org.example.k_market.dao.CmStoryDAO;   // 본인 패키지 경로에 맞게 매칭
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Log4j2
@RequiredArgsConstructor
@Controller
@RequestMapping("/company")
public class CompanyController {

    // 💡 중간 서비스 없이 창고지기(DAO)들을 직접 호출하도록 주입!
    private final CmStoryDAO cmStoryDAO;
    private final CmRecruitDAO cmRecruitDAO;
    private final CmMediaDAO cmMediaDAO;

    // 1. HOME (회사소개 메인) - 정적 페이지인 경우 그대로 유지
    @GetMapping({"/index", "/main"})
    public String main() {
        log.info("CompanyController - 메인 화면 이동");
        return "company/main";
    }

    // 2. 기업문화 - 정적 페이지인 경우 그대로 유지
    @GetMapping("/culture")
    public String culture() {
        log.info("CompanyController - 기업문화 이동");
        return "company/culture";
    }

    // 3. 소식과 이야기
    @GetMapping("/story")
    public String story(Model model) {
        log.info("CompanyController - 소식과 이야기 데이터 조회");

        // DAO에 작성해두신 전체 조회 메서드명으로 호출하세요 (예: selectCmStories, selectAll 등)
        // 여기서는 예시로 selectCmStories()를 호출했습니다.
        model.addAttribute("stories", cmStoryDAO.findAll());

        return "company/story";
    }

    // 4. 채용
    @GetMapping("/recruit")
    public String recruit(Model model) {
        log.info("CompanyController - 채용 공고 데이터 조회");

        // DAO에 정의된 채용 목록 조회 메서드 호출 (예: selectCmRecruits)
        model.addAttribute("recruits", cmRecruitDAO.findAll());

        return "company/recruit";
    }

    // 5. 미디어
    @GetMapping("/media")
    public String media(Model model) {
        log.info("CompanyController - 미디어 데이터 조회");

        // DAO에 정의된 미디어 목록 조회 메서드 호출 (예: selectCmMedias)
        model.addAttribute("medias", cmMediaDAO.findAll());

        return "company/media";
    }
}