package org.example.k_market.service;

import lombok.RequiredArgsConstructor;
import org.example.k_market.dto.ProductDTO;
import org.example.k_market.entity.Product;
import org.example.k_market.repository.ProductRepository;
import org.example.k_market.service.aws.S3Uploader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final String PRODUCT_IMAGE_DIR = "products";
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;

    private final ProductRepository productRepository;
    private final S3Uploader s3Uploader;

    public List<ProductDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(Product::toDTO)
                .toList();
    }

    public Product findById(Long prodNo) {
        if (prodNo == null) {
            return null;
        }

        return productRepository.findById(prodNo)
                .orElse(null);
    }

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

    @Transactional
    public void delete(Long prodNo) {
        Product product = productRepository.findById(prodNo)
                .orElseThrow(() ->
                        new IllegalArgumentException("삭제할 상품을 찾을 수 없습니다.")
                );

        deleteProductImages(product);
        productRepository.delete(product);
    }

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

    private String uploadIfPresent(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        return s3Uploader.upload(file, PRODUCT_IMAGE_DIR);
    }

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

    private void deleteProductImages(Product product) {
        deleteS3Image(product.getThumb1());
        deleteS3Image(product.getThumb2());
        deleteS3Image(product.getThumb3());
        deleteS3Image(product.getDetailContent());
    }

    private void deleteS3Image(String imageUrl) {
        if (StringUtils.hasText(imageUrl)
                && imageUrl.startsWith("http")) {
            s3Uploader.deleteFile(imageUrl);
        }
    }
}
