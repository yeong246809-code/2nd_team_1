package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.entity.Banner;
import org.example.k_market.repository.BannerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final String UPLOAD_PATH = new File("uploads/").getAbsolutePath() + File.separator;

    public List<BannerDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(Banner::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerBanner(BannerDTO dto, MultipartFile file) throws IOException {
        // 1. 파일 업로드 처리
        if (!file.isEmpty()) {
            String storedFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            file.transferTo(new File(UPLOAD_PATH + storedFileName));
            dto.setFileUrl(storedFileName); // DB에는 저장된 파일명만 저장
        }

        // 2. 초기 상태를 '비활성'으로 설정
        dto.setStatus("비활성");

        bannerRepository.save(dto.toEntity());
    }

    @Transactional
    public void toggleStatus(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));

        String newStatus = banner.getStatus().equals("활성") ? "비활성" : "활성";

        banner.updateStatus(newStatus);
    }
}