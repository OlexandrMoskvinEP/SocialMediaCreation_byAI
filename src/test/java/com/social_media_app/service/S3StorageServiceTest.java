package com.social_media_app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    S3Client s3;
    S3Presigner presigner;
    S3StorageService service;

    @BeforeEach
    void setUp() {
        s3 = mock(S3Client.class);
        presigner = mock(S3Presigner.class);
        service = new S3StorageService(s3, presigner, "test-bucket");
    }

    @Test
    void upload_putsObjectAndReturnsKeyWithPrefix() throws Exception {
        // given
        var file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "bytes".getBytes());

        // when
        String key = service.upload("uploads/alex", file);

        // then
        assertNotNull(key);
        assertTrue(key.startsWith("uploads/alex/"), "key should start with prefix");

        // capture the request sent to S3
        ArgumentCaptor<PutObjectRequest> putReq = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3, times(1)).putObject(putReq.capture(), any(RequestBody.class));

        assertEquals("test-bucket", putReq.getValue().bucket());
        assertEquals("image/jpeg", putReq.getValue().contentType());
        assertEquals(key, putReq.getValue().key()); // service-generated key used in request
    }

    @Test
    void presignedUrl_returnsUrlFromPresigner() throws Exception {
        // given
        var presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(new URL("https://example.com/object?sig=123"));
        when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        // when
        var url = service.presignedUrl("uploads/alex/obj", Duration.ofMinutes(30));

        // then
        assertEquals("https://example.com/object?sig=123", url.toString());
        verify(presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
    }
}
