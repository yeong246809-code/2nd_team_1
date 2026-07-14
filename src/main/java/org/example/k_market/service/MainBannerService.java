package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.entity.Banner;
import org.example.k_market.repository.BannerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainBannerService {
    private static final String TOP_POSITION = "MAIN1";
    private static final String SLIDER_POSITION = "MAIN2";
    private static final String LEFT_SIDE_POSITION = "MAIN3";
    private static final String RIGHT_SIDE_POSITION = "MAIN4";
    private static final int MAX_SLIDER_BANNERS = 6;

    private final BannerRepository bannerRepository;

    public Optional<BannerDTO> findTopBanner() {
        return findBanners(TOP_POSITION, 1, "1200 / 100").stream().findFirst();
    }

    public List<BannerDTO> findSliderBanners() {
        return findBanners(SLIDER_POSITION, MAX_SLIDER_BANNERS, "16 / 6");
    }

    public Optional<BannerDTO> findLeftSideBanner() {
        return findBanners(LEFT_SIDE_POSITION, 1, "1 / 2").stream().findFirst();
    }

    public Optional<BannerDTO> findRightSideBanner() {
        return findBanners(RIGHT_SIDE_POSITION, 1, "1 / 2").stream().findFirst();
    }

    private List<BannerDTO> findBanners(String position, int limit, String fallbackAspectRatio) {
        return bannerRepository.findDisplayableByPosition(
                        position, LocalDate.now(), LocalTime.now(), PageRequest.of(0, limit))
                .stream()
                .map(Banner::toDTO)
                .peek(banner -> normalize(banner, fallbackAspectRatio))
                .toList();
    }

    private void normalize(BannerDTO banner, String fallbackAspectRatio) {
        normalizeAspectRatio(banner, fallbackAspectRatio);
        String stored = banner.getFileUrl_stored();
        if (StringUtils.hasText(stored)
                && !stored.startsWith("http://") && !stored.startsWith("https://") && !stored.startsWith("/")) {
            banner.setFileUrl_stored("/uploads/" + stored);
        }

        String linkUrl = banner.getLinkUrl();
        if (StringUtils.hasText(linkUrl)
                && !linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")
                && !linkUrl.startsWith("/") && !linkUrl.startsWith("#")) {
            banner.setLinkUrl("/" + linkUrl);
        }
    }

    private void normalizeAspectRatio(BannerDTO banner, String fallbackAspectRatio) {
        String sizeType = banner.getSizeType();
        if (!StringUtils.hasText(sizeType)) {
            banner.setAspectRatio(fallbackAspectRatio);
            return;
        }
        String[] parts = sizeType.toLowerCase().replace(" ", "").split("x");
        if (parts.length == 2 && parts[0].matches("\\d+") && parts[1].matches("\\d+")) {
            banner.setAspectRatio(parts[0] + " / " + parts[1]);
        } else {
            banner.setAspectRatio(fallbackAspectRatio);
        }
    }
}
