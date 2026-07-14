package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.BannerDTO;
import org.example.k_market.dto.PageResponseDTO;
import org.example.k_market.entity.Banner;
import org.example.k_market.repository.BannerRepository;
import org.example.k_market.service.aws.S3Uploader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private static final String ACTIVE = "활성";
    private static final String INACTIVE = "비활성";

    private final BannerRepository bannerRepository;
    private final S3Uploader s3Uploader;


    /**
     * 사용자 메인 화면에 노출할 배너 목록을 조회한다.
     *
     * 활성 상태이고 현재 날짜/시간이 노출 기간 안에 있으며,
     * 실제 이미지 주소가 등록된 배너만 조회한다.
     *
     * @param position 배너 위치 코드 (예: MAIN1, MAIN2)
     * @param limit    최대 조회 개수
     * @return 노출 가능한 배너 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<BannerDTO> getDisplayableBanners(String position, int limit) {

        int safeLimit = Math.max(limit, 1);

        return bannerRepository.findDisplayableByPosition(
                        position,
                        LocalDate.now(),
                        LocalTime.now(),
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(Banner::toDTO)
                .toList();
    }

    /**
     * 지정한 위치에서 가장 최근에 등록된 노출 가능 배너 1개를 조회한다.
     *
     * @param position 배너 위치 코드
     * @return 노출 가능한 배너 1개, 없으면 null
     */
    @Transactional(readOnly = true)
    public BannerDTO getDisplayableBanner(String position) {
        return getDisplayableBanners(position, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    // 1. 페이징 + 위치 필터링 + 정렬이 적용된 목록 조회
    public PageResponseDTO<BannerDTO> getBannerList(String position, String sort, int pg) {
        // 1페이지당 5개씩 조회 (JPA는 0부터 시작하므로 pg - 1)
        Pageable pageable = PageRequest.of(pg - 1, 5);
        Page<Banner> pageResult;

        // 선택된 정렬 기준에 따라 쿼리 메서드 호출
        if ("idAsc".equals(sort)) {
            pageResult = bannerRepository.findByPositionOrderByIdAsc(position, pageable);
        } else if ("dateDesc".equals(sort)) {
            pageResult = bannerRepository.findByPositionOrderByDateDesc(position, pageable);
        } else {
            // 기본값: ID 내림차순 (idDesc)
            pageResult = bannerRepository.findByPositionOrderByIdDesc(position, pageable);
        }

        // Entity Page를 DTO Page로 변환
        Page<BannerDTO> dtoPage = pageResult.map(Banner::toDTO);

        // 공통 PageResponseDTO(블록 사이즈 5)로 감싸서 반환
        return new PageResponseDTO<>(dtoPage, 5);
    }

    // 2. 배너 등록 (에러가 났던 바로 그 메서드!)
    @Transactional
    public void registerBanner(BannerDTO dto, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            dto.setFileUrl(file.getOriginalFilename());
            dto.setFileUrl_stored(s3Uploader.upload(file, "banners"));
        }

        dto.setStatus(INACTIVE);
        bannerRepository.save(dto.toEntity());
    }

    // 3. 상태(활성/비활성) 변경
    @Transactional
    public void toggleStatus(Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("배너를 찾을 수 없습니다."));

        String newStatus = ACTIVE.equals(banner.getStatus()) ? INACTIVE : ACTIVE;
        banner.updateStatus(newStatus);
    }

    // 4. 선택 삭제
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