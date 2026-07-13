package org.example.k_market.service.member;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.entity.Banner;
import org.example.k_market.repository.BannerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyBannerService {

    private static final String MY_PAGE_POSITION = "MY1";

    private final BannerRepository bannerRepository;

    public List<BannerDTO> findMyPageBanners() {
        return bannerRepository.findDisplayableByPosition(
                        MY_PAGE_POSITION,
                        LocalDate.now(),
                        LocalTime.now(),
                        PageRequest.of(0, 10)
                )
                .stream()
                .map(Banner::toDTO)
                .peek(this::normalizeImageUrl)
                .toList();
    }

    private void normalizeImageUrl(BannerDTO banner) {
        normalizeAspectRatio(banner);
        String stored = banner.getFileUrl_stored();
        if (!StringUtils.hasText(stored)) {
            return;
        }
        if (stored.startsWith("http://") || stored.startsWith("https://") || stored.startsWith("/")) {
            normalizeLinkUrl(banner);
            return;
        }
        banner.setFileUrl_stored("/uploads/" + stored);
        normalizeLinkUrl(banner);
    }

    private void normalizeLinkUrl(BannerDTO banner) {
        String linkUrl = banner.getLinkUrl();
        if (!StringUtils.hasText(linkUrl)) {
            return;
        }
        if (linkUrl.startsWith("http://") || linkUrl.startsWith("https://") || linkUrl.startsWith("/") || linkUrl.startsWith("#")) {
            return;
        }
        banner.setLinkUrl("/" + linkUrl);
    }

    private void normalizeAspectRatio(BannerDTO banner) {
        String sizeType = banner.getSizeType();
        if (!StringUtils.hasText(sizeType)) {
            banner.setAspectRatio("810 / 86");
            return;
        }
        String normalized = sizeType.toLowerCase().replace(" ", "");
        String[] parts = normalized.split("x");
        if (parts.length != 2 || !parts[0].matches("\\d+") || !parts[1].matches("\\d+")) {
            banner.setAspectRatio("810 / 86");
            return;
        }
        banner.setAspectRatio(parts[0] + " / " + parts[1]);
    }
}
