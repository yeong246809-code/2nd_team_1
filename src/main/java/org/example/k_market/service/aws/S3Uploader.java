package org.example.k_market.service.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        if (!StringUtils.hasText(bucket)) {
            throw new IOException("AWS S3 bucket is not configured.");
        }

        String originalFileName = multipartFile.getOriginalFilename();
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + originalFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));

            return amazonS3.getUrl(bucket, fileName).toString();
        } catch (SdkClientException e) {
            throw new IOException("AWS S3 upload failed.", e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            String fileName = new URL(fileUrl).getPath().replaceFirst("^/", "");
            amazonS3.deleteObject(bucket, fileName);
        } catch (Exception e) {
            System.out.println("S3 파일 삭제 실패: " + e.getMessage());
        }
    }
}
