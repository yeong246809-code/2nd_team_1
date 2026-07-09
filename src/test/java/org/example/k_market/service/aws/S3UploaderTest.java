package org.example.k_market.service.aws;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class S3UploaderTest {

    @Test
    void uploadFailsBeforeCallingAwsWhenBucketIsMissing() {
        AmazonS3 amazonS3 = mock(AmazonS3.class);
        S3Uploader uploader = new S3Uploader(amazonS3);
        ReflectionTestUtils.setField(uploader, "bucket", "");

        assertThrows(IOException.class, () -> uploader.upload(imageFile(), "banners"));
        verifyNoInteractions(amazonS3);
    }

    @Test
    void uploadConvertsAwsCredentialFailureToIOException() {
        AmazonS3 amazonS3 = mock(AmazonS3.class);
        S3Uploader uploader = new S3Uploader(amazonS3);
        ReflectionTestUtils.setField(uploader, "bucket", "test-bucket");
        doThrow(new SdkClientException("credentials unavailable"))
                .when(amazonS3)
                .putObject(any());

        IOException exception = assertThrows(
                IOException.class,
                () -> uploader.upload(imageFile(), "banners")
        );

        assertInstanceOf(SdkClientException.class, exception.getCause());
    }

    private MockMultipartFile imageFile() {
        return new MockMultipartFile(
                "file",
                "banner.png",
                "image/png",
                new byte[]{1}
        );
    }
}
