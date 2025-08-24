package com.social_media_app.controller;

import com.social_media_app.service.S3StorageService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final S3StorageService s3;

    public MediaController(S3StorageService s3) {
        this.s3 = s3;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestPart("file") @NotNull MultipartFile file,
                                      Authentication auth) throws Exception {
        String username = auth != null ? auth.getName() : "anonymous";
        String key = s3.upload("uploads/" + username, file);
        URL url = s3.presignedUrl(key, Duration.ofHours(1));

        return Map.of("key", key, "url", url.toString());
    }
}
