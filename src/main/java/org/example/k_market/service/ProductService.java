package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.ProductDTO;
import org.example.k_market.entity.Product;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.repository.ReviewRepository;
import org.example.k_market.service.aws.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 상품 비즈니스 로직을 담당하는 서비스 클래스.
 *
 * 주요 기능
 * - 상품 전체 조회 및 단건 조회
 * - 상품 등록, 수정, 삭제
 * - 상품 이미지 S3 업로드 및 삭제
 * - 메인 페이지 상품 목록 조회
 * - 상품 검색
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    /** S3에 상품 이미지를 저장할 기본 폴더명 */
    private static final String PRODUCT_IMAGE_DIR = "products";

    /** 상품 이미지 1장당 최대 허용 크기: 10MB */
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;

    private final ProductRepository productRepository;
    private final S3Uploader s3Uploader;

    /**
     * 전체 상품을 DTO 목록으로 조회한다.
     */
    public List<ProductDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(Product::toDTO)
                .toList();
    }

    /**
     * 상품 번호로 상품을 조회한다.
     *
     * @param prodNo 상품 번호
     * @return 조회된 상품, 상품 번호가 없거나 상품을 찾지 못하면 null
     */
    public Product findById(Long prodNo) {
        if (prodNo == null) {
            return null;
        }

        return productRepository.findById(prodNo)
                .orElse(null);
    }

    /**
     * 새 상품을 등록한다.
     *
     * 전달받은 이미지 파일을 검증한 뒤 S3에 업로드하고,
     * 업로드된 URL을 상품 DTO에 저장하여 상품 엔티티를 생성한다.
     */
    @Transactional
    public Product register(
            ProductDTO productDTO,
            MultipartFile file1,
            MultipartFile file2,
            MultipartFile file3,
            MultipartFile detailContent) throws IOException {

        validateImageFiles(file1, file2, file3, detailContent);

        productDTO.setThumb1(uploadIfPresent(file1));
        productDTO.setThumb2(uploadIfPresent(file2));
        productDTO.setThumb3(uploadIfPresent(file3));
        productDTO.setDetailContent(uploadIfPresent(detailContent));

        Product product = productDTO.toEntity();

        return productRepository.save(product);
    }

    /**
     * 기존 상품 정보를 수정한다.
     *
     * 새 이미지가 전달된 경우에만 기존 이미지를 교체하며,
     * 새 이미지가 없으면 기존 이미지 URL을 그대로 유지한다.
     */
    @Transactional
    public Product modify(
            ProductDTO productDTO,
            MultipartFile file1,
            MultipartFile file2,
            MultipartFile file3,
            MultipartFile detailContent) throws IOException {

        if (productDTO.getProdNo() == null) {
            throw new IllegalArgumentException("상품 번호가 없습니다.");
        }

        Product savedProduct = productRepository.findById(productDTO.getProdNo())
                .orElseThrow(() ->
                        new IllegalArgumentException("수정할 상품을 찾을 수 없습니다.")
                );

        validateImageFiles(file1, file2, file3, detailContent);
        copyEditableFields(productDTO, savedProduct);

        savedProduct.setThumb1(
                replaceImage(savedProduct.getThumb1(), file1)
        );
        savedProduct.setThumb2(
                replaceImage(savedProduct.getThumb2(), file2)
        );
        savedProduct.setThumb3(
                replaceImage(savedProduct.getThumb3(), file3)
        );
        savedProduct.setDetailContent(
                replaceImage(savedProduct.getDetailContent(), detailContent)
        );

        return productRepository.save(savedProduct);
    }

    /**
     * 상품 1개를 삭제한다.
     *
     * DB 데이터 삭제 전에 해당 상품이 사용하던 S3 이미지도 함께 삭제한다.
     */
    @Transactional
    public void delete(Long prodNo) {
        Product product = productRepository.findById(prodNo)
                .orElseThrow(() ->
                        new IllegalArgumentException("삭제할 상품을 찾을 수 없습니다.")
                );

        deleteProductImages(product);
        productRepository.delete(product);
    }

    /**
     * 선택한 여러 상품을 일괄 삭제한다.
     *
     * 존재하지 않는 상품 번호는 건너뛰고,
     * 실제로 조회된 상품만 이미지와 DB 데이터를 삭제한다.
     */
    @Transactional
    public void deleteAll(List<Long> prodNos) {
        if (prodNos == null || prodNos.isEmpty()) {
            throw new IllegalArgumentException("삭제할 상품을 선택해 주세요.");
        }

        for (Long prodNo : prodNos) {
            Product product = productRepository.findById(prodNo)
                    .orElse(null);

            if (product == null) {
                continue;
            }

            deleteProductImages(product);
            productRepository.delete(product);
        }
    }

    /**
     * 수정 가능한 일반 상품 필드를 기존 엔티티에 복사한다.
     *
     * 이미지 필드는 S3 교체 처리가 필요하므로 이 메서드에서 제외한다.
     */
    private void copyEditableFields(ProductDTO source, Product target) {
        target.setCateNo(source.getCateNo());
        target.setShopNo(source.getShopNo());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setManufacturer(source.getManufacturer());
        target.setPrice(source.getPrice());
        target.setDiscountRate(source.getDiscountRate());
        target.setRewardPoints(source.getRewardPoints());
        target.setStockQuantity(source.getStockQuantity());
        target.setShippingFee(source.getShippingFee());
        target.setStatus(source.getStatus());
        target.setTaxFreeYn(source.getTaxFreeYn());
        target.setReceiptYn(source.getReceiptYn());
        target.setBizType(source.getBizType());
        target.setOrigin(source.getOrigin());

        // 조회수, 판매량, 평점, 등록일은 값이 전달된 경우에만 변경한다.
        if (source.getViewCount() != null) {
            target.setViewCount(source.getViewCount());
        }
        if (source.getSalesCount() != null) {
            target.setSalesCount(source.getSalesCount());
        }
        if (source.getRating() != null) {
            target.setRating(source.getRating());
        }
        if (source.getCreatedAt() != null) {
            target.setCreatedAt(source.getCreatedAt());
        }
    }

    /**
     * 파일이 전달된 경우에만 S3에 업로드한다.
     */
    private String uploadIfPresent(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return s3Uploader.upload(file, PRODUCT_IMAGE_DIR);
    }

    /**
     * 기존 이미지를 새 이미지로 교체한다.
     *
     * 새 파일이 없으면 기존 URL을 유지하고,
     * 새 파일이 있으면 먼저 업로드한 뒤 기존 이미지를 삭제한다.
     */
    private String replaceImage(
            String oldImageUrl,
            MultipartFile newImage) throws IOException {

        if (newImage == null || newImage.isEmpty()) {
            return oldImageUrl;
        }

        String newImageUrl = s3Uploader.upload(
                newImage,
                PRODUCT_IMAGE_DIR
        );

        deleteS3Image(oldImageUrl);

        return newImageUrl;
    }

    /**
     * 업로드된 파일이 이미지인지, 허용 크기 이하인지 검사한다.
     */
    private void validateImageFiles(MultipartFile... files) {
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType();

            if (!StringUtils.hasText(contentType)
                    || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException(
                        "이미지 파일만 업로드할 수 있습니다."
                );
            }

            if (file.getSize() > MAX_IMAGE_SIZE) {
                throw new IllegalArgumentException(
                        "이미지는 한 장당 10MB 이하만 업로드할 수 있습니다."
                );
            }
        }
    }

    /**
     * 상품에 연결된 모든 이미지를 S3에서 삭제한다.
     */
    private void deleteProductImages(Product product) {
        deleteS3Image(product.getThumb1());
        deleteS3Image(product.getThumb2());
        deleteS3Image(product.getThumb3());
        deleteS3Image(product.getDetailContent());
    }

    /**
     * 유효한 S3 이미지 URL인 경우에만 파일을 삭제한다.
     */
    private void deleteS3Image(String imageUrl) {
        if (StringUtils.hasText(imageUrl)
                && imageUrl.startsWith("http")) {
            s3Uploader.deleteFile(imageUrl);
        }
    }

    /**
     * 메인 페이지 베스트 상품을 조회한다.
     * 판매량(salesCount)이 높은 순서대로 최대 5개를 반환한다.
     */
    public List<Product> getBestProducts() {
        return productRepository.findTop5ByOrderBySalesCountDesc();
    }

    /**
     * 메인 페이지 히트 상품을 조회한다.
     * 조회수(viewCount)가 높은 순서대로 최대 8개를 반환한다.
     */
    public List<Product> getHitProducts() {
        return productRepository.findTop8ByOrderByViewCountDesc();
    }

    /**
     * 메인 페이지 추천 상품을 조회한다.
     *
     * 현재 상품 테이블에 추천 여부 컬럼이 없으므로
     * 평점(rating)이 높은 순서대로 최대 8개를 추천 상품으로 사용한다.
     */
    public List<Product> getRecommendedProducts() {
        return productRepository.findTop8ByOrderByRatingDesc();
    }

    /**
     * 메인 페이지 최신 상품을 조회한다.
     * 등록일(createdAt)이 최근인 순서대로 최대 8개를 반환한다.
     */
    public List<Product> getLatestProducts() {
        return productRepository.findTop8ByOrderByCreatedAtDesc();
    }

    /**
     * 메인 페이지 할인 상품을 조회한다.
     * 할인율(discountRate)이 높은 순서대로 최대 8개를 반환한다.
     */
    public List<Product> getDiscountProducts() {
        return productRepository.findTop8ByOrderByDiscountRateDesc();
    }

    /**
     * 상품명 또는 상품 설명으로 상품을 검색한다.
     *
     * 검색어가 비어 있으면 전체 상품을 반환하고,
     * 검색어가 있으면 상품명 또는 설명에 검색어가 포함된 상품만 반환한다.
     */
    public List<Product> searchProducts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return productRepository.findAll();
        }

        String trimmedKeyword = keyword.trim();

        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        trimmedKeyword,
                        trimmedKeyword
                );
    }

    private final ReviewRepository reviewRepository;

    /**
     * 특정 상품의 리뷰 평균 평점을 다시 계산하여 저장한다.
     *
     * review 테이블의 rating 평균을 구한 뒤
     * product.rating 컬럼에 소수점 둘째 자리까지 저장한다.
     */
    @Transactional
    public void updateProductRating(Long prodNo) {

        Product product = productRepository.findById(prodNo)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "상품을 찾을 수 없습니다: " + prodNo
                        )
                );

        Double averageRating =
                reviewRepository.findAverageRatingByProdNo(prodNo);

        /*
         * 등록된 리뷰가 없으면 평점을 null로 처리한다.
         */
        if (averageRating == null) {
            product.setRating(null);
        } else {
            product.setRating(
                    BigDecimal.valueOf(averageRating)
                            .setScale(2, RoundingMode.HALF_UP)
            );
        }

        productRepository.save(product);
    }

}