package org.example.k_market.service.aws;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_AWS_S3_INTEGRATION_TEST", matches = "true")
class S3IntegrationTest {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final S3Uploader uploader;
    private final AmazonS3 amazonS3;

    @Autowired
    S3IntegrationTest(S3Uploader uploader, AmazonS3 amazonS3) {
        this.uploader = uploader;
        this.amazonS3 = amazonS3;
    }

    @Test
    void uploadsAndDeletesSmallFile() throws Exception {
        String fileUrl = uploader.upload(
                new MockMultipartFile(
                        "file",
                        "codex-s3-check.txt",
                        "text/plain",
                        new byte[]{1}
                ),
                "connection-checks"
        );
        assertNotNull(fileUrl);

        String objectKey = URI.create(fileUrl).getPath().replaceFirst("^/", "");
        try {
            HttpResponse<Void> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(fileUrl)).GET().build(),
                    HttpResponse.BodyHandlers.discarding()
            );
            assertEquals(200, response.statusCode());
        } finally {
            amazonS3.deleteObject(bucket, objectKey);
        }

        assertFalse(amazonS3.doesObjectExist(bucket, objectKey));
    }
}
