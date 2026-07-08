package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.entity.Banner;
import org.example.k_market.repository.BannerRepository;
import org.example.k_market.service.aws.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;
    private final S3Uploader s3Uploader; // S3 업로더 추가

    public List<BannerDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(Banner::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerBanner(BannerDTO dto, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            dto.setFileUrl(file.getOriginalFilename());

            // 로컬 폴더 대신 S3의 'banners' 폴더에 업로드
            String storedFileUrl = s3Uploader.upload(file, "banners");

            // DTO에 파일 이름이 아닌 S3 전체 URL을 저장
            dto.setFileUrl_stored(storedFileUrl);
        }

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

    @Transactional
    public void deleteBanners(List<Long> bannerIds) {
        for (Long id : bannerIds) {
            bannerRepository.findById(id).ifPresent(banner -> {
                if (banner.getFileUrl_stored() != null) {
                    // S3에서도 실제 파일 삭제
                    s3Uploader.deleteFile(banner.getFileUrl_stored());
                }
            });
        }
        bannerRepository.deleteAllById(bannerIds);
    }
}