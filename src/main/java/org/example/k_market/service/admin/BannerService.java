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

@Service
@RequiredArgsConstructor
public class BannerService {

    private static final String ACTIVE = "활성";
    private static final String INACTIVE = "비활성";

    private final BannerRepository bannerRepository;
    private final S3Uploader s3Uploader;

    public List<BannerDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .map(Banner::toDTO)
                .toList();
    }

    @Transactional
    public void registerBanner(BannerDTO dto, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            dto.setFileUrl(file.getOriginalFilename());
            dto.setFileUrl_stored(s3Uploader.upload(file, "banners"));
        }

        dto.setStatus(INACTIVE);
        bannerRepository.save(dto.toEntity());
    }

    @Transactional
    public void toggleStatus(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));

        String newStatus = ACTIVE.equals(banner.getStatus()) ? INACTIVE : ACTIVE;
        banner.updateStatus(newStatus);
    }

    @Transactional
    public void deleteBanners(List<Long> bannerIds) {
        for (Long id : bannerIds) {
            bannerRepository.findById(id).ifPresent(banner -> {
                if (banner.getFileUrl_stored() != null) {
                    s3Uploader.deleteFile(banner.getFileUrl_stored());
                }
            });
        }
        bannerRepository.deleteAllById(bannerIds);
    }
}
