package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.SiteConfigDTO;
import org.example.k_market.entity.SiteConfig;
import org.example.k_market.entity.Version;
import org.example.k_market.repository.SiteConfigRepository;
import org.example.k_market.repository.VersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SiteConfigService {

    private final SiteConfigRepository siteConfigRepository;
    private final VersionRepository versionRepository;

    // 일반 이미지 경로
    private final String UPLOAD_PATH = new File("uploads/").getAbsolutePath() + File.separator;

    // 1. 단일 설정 데이터 가져오기 (무조건 Update를 위해, 없으면 빈 객체 생성)
    public SiteConfigDTO getSiteConfig() {
        SiteConfig config = siteConfigRepository.findById(1).orElse(new SiteConfig());
        return config.getId() == 0 ? new SiteConfigDTO() : config.toDTO();
    }

    // 2. 가장 최신 사이트 버전 가져오기
    public String getLatestVersionCode() {
        Version latestVersion = versionRepository.findTopByOrderByIdDesc();
        String s = (latestVersion != null) ? latestVersion.getVersionCode() : null;
        return s;
    }

    // --- 각 파트별 부분 Update 로직 (무조건 Update) ---
    // 엔티티를 찾아서 넘겨받은 값만 Setter로 변경하고 저장합니다. (더티체킹)

    // 1. 사이트 정보 업데이트
    @Transactional
    public void updateSite(SiteConfigDTO dto) {
        SiteConfig config = getOrCreateConfig();
        // 필요한 값만 갈아끼우면 트랜잭션 종료 시 자동 UPDATE (더티 체킹)
        config.setTitle(dto.getTitle());
        config.setSubtitle(dto.getSubtitle());
    }

    // 2. 기업 정보 업데이트
    @Transactional
    public void updateCompany(SiteConfigDTO dto) {
        SiteConfig config = getOrCreateConfig();
        config.setCompanyName(dto.getCompanyName());
        config.setCeo(dto.getCeo());
        config.setBizNumber(dto.getBizNumber());
        config.setMailOrderNumber(dto.getMailOrderNumber());
        config.setBaseAddress(dto.getBaseAddress());
        config.setDetailAddress(dto.getDetailAddress());
    }

    // 3. 고객센터 정보 업데이트
    @Transactional
    public void updateCs(SiteConfigDTO dto) {
        SiteConfig config = getOrCreateConfig();
        config.setCsPhone(dto.getCsPhone());
        config.setCsHours(dto.getCsHours());
        config.setCsEmail(dto.getCsEmail());
        config.setFinDisputeManager(dto.getFinDisputeManager());
    }

    // 4. 카피라이트 정보 업데이트
    @Transactional
    public void updateCopyright(SiteConfigDTO dto) {
        SiteConfig config = getOrCreateConfig();
        config.setCopyright(dto.getCopyright());
    }

    // 5. 로고 파일 업데이트 (_origin, _stored 분리 적용)
    @Transactional
    public void updateLogos(MultipartFile header, MultipartFile footer, MultipartFile favicon) throws IOException {
        SiteConfig config = getOrCreateConfig();

        // 업로드 경로 확인
        File dir = new File(UPLOAD_PATH);
        if(!dir.exists()) dir.mkdirs();

        // 1. 헤더 로고 업데이트
        if (header != null && !header.isEmpty()) {
            config.setHeaderLogo_origin(header.getOriginalFilename());
            config.setHeaderLogo_stored(resizeAndSave(header, 370, null));
        }

        // 2. 푸터 로고 업데이트
        if (footer != null && !footer.isEmpty()) {
            config.setFooterLogo_origin(footer.getOriginalFilename());
            config.setFooterLogo_stored(resizeAndSave(footer, 155, null));
        }

        // 3. 파비콘 업데이트 (이제 uploads/ 폴더에서 관리)
        if (favicon != null && !favicon.isEmpty()) {
            config.setFavicon_origin(favicon.getOriginalFilename());

            // 32x32 사이즈로 리사이징 후 저장
            String savedFaviconName = resizeAndSave(favicon, 32, 32);
            config.setFavicon_stored(savedFaviconName);

            System.out.println("★ 파비콘 저장 성공: " + savedFaviconName);
        }

        // 별도의 save() 없이도 트랜잭션 종료 시 JPA가 알아서 변경사항을 DB에 반영합니다.
    }

    private SiteConfig getOrCreateConfig() {
        return siteConfigRepository.findById(1)
                .orElse(SiteConfig.builder().id(1).build());
    }

    private String resizeAndSave(MultipartFile file, Integer targetWidth, Integer targetHeight) throws IOException {
        // 1. 업로드된 파일 읽기
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int newWidth = targetWidth != null ? targetWidth : originalImage.getWidth();
        int newHeight = targetHeight != null ? targetHeight : originalImage.getHeight();

        if (targetWidth != null && targetHeight == null) {
            newHeight = (int) (originalImage.getHeight() * ((double) targetWidth / originalImage.getWidth()));
        }

        Image resultingImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // ★ 확장자 확인
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        boolean isPng = ext.equals("png");

        // ★ 핵심 수정: PNG면 투명도 지원(ARGB), 아니면 일반(RGB)으로 도화지 생성
        int imageType = isPng ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight, imageType);

        Graphics2D g2d = outputImage.createGraphics();

        // JPG일 경우 배경이 까맣게 나오는 것을 방지하기 위해 흰색으로 채움
        if (!isPng) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, newWidth, newHeight);
        }

        g2d.drawImage(resultingImage, 0, 0, null);
        g2d.dispose();

        String savedName = UUID.randomUUID().toString() + "." + ext;

        // 폴더 확인 및 생성
        File dir = new File(UPLOAD_PATH);
        if(!dir.exists()) dir.mkdirs();

        File outputFile = new File(dir, savedName);

        // ★ 콘솔에 저장되는 절대 경로 출력 (눈으로 직접 확인하세요!)
        System.out.println("=========================================");
        System.out.println("★ 이미지 저장 시도 경로: " + outputFile.getAbsolutePath());
        System.out.println("=========================================");

        // 파일 저장 실행
        boolean isSuccess = ImageIO.write(outputImage, isPng ? "png" : "jpg", outputFile);

        if(!isSuccess) {
            System.out.println("★ 저장 실패: " + ext + " 포맷을 저장할 수 없습니다.");
        }

        return savedName;
    }

    private void saveAsIco(BufferedImage image, File outputFile) throws IOException {
        // 1. 이미지를 메모리상에서 PNG 바이트 배열로 변환
        ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", pngStream);
        byte[] pngBytes = pngStream.toByteArray();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // 2. 표준 ICO 헤더 작성 (22 바이트)
            fos.write(new byte[]{0, 0}); // Reserved
            fos.write(new byte[]{1, 0}); // Type: 1 (ICO)
            fos.write(new byte[]{1, 0}); // Image count: 1

            // Image Directory
            int width = image.getWidth();
            int height = image.getHeight();
            fos.write((byte) (width >= 256 ? 0 : width));
            fos.write((byte) (height >= 256 ? 0 : height));
            fos.write(0); // Color palette
            fos.write(0); // Reserved
            fos.write(new byte[]{1, 0}); // Color planes
            fos.write(new byte[]{32, 0}); // Bits per pixel (32 for ARGB)

            int size = pngBytes.length;
            fos.write(new byte[]{(byte) size, (byte) (size >> 8), (byte) (size >> 16), (byte) (size >> 24)});

            int offset = 22; // 헤더 이후 데이터 시작 위치
            fos.write(new byte[]{(byte) offset, (byte) (offset >> 8), (byte) (offset >> 16), (byte) (offset >> 24)});

            // 3. 실제 PNG 데이터 덮어쓰기
            fos.write(pngBytes);
        }
    }
}
