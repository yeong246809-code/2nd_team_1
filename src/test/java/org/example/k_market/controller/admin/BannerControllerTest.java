package org.example.k_market.controller.admin;

import org.example.k_market.dto.BannerDTO;
import org.example.k_market.service.admin.BannerService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class BannerControllerTest {

    @Test
    void addBannerRedirectsWithMessageWhenUploadFails() throws IOException {
        BannerService bannerService = mock(BannerService.class);
        BannerController controller = new BannerController(bannerService);
        RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

        doThrow(new IOException("S3 upload failed"))
                .when(bannerService)
                .registerBanner(any(BannerDTO.class), any());

        String view = controller.addBanner(
                new BannerDTO(),
                new MockMultipartFile("file", "banner.png", "image/png", new byte[]{1}),
                redirectAttributes
        );

        assertEquals("redirect:/admin/config/banner", view);
        assertEquals(
                "배너 파일 업로드 중 오류가 발생했습니다.",
                redirectAttributes.getFlashAttributes().get("message")
        );
    }
}
