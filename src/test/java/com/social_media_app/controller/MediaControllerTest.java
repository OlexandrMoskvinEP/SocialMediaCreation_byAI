package com.social_media_app.controller;

import com.social_media_app.service.S3StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MediaController.class)
@Import(com.social_media_app.config.SecurityConfig.class)
class MediaControllerWithSecurityTest {
    @Autowired
    MockMvc mvc;
    @MockitoBean
    S3StorageService s3;
    @MockitoBean
    com.social_media_app.security.JwtFilter jwtFilter;

    @BeforeEach
    void passThrough() throws Exception {
        doAnswer(inv -> {
            var req = inv.getArgument(0, jakarta.servlet.ServletRequest.class);
            var res = inv.getArgument(1, jakarta.servlet.ServletResponse.class);
            var chain = inv.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(req, res);

            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void upload_anonymous_returnsKeyAndUrl() throws Exception {
        var file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "bytes".getBytes());

        when(s3.upload(eq("uploads/anonymous"), any()))
                .thenReturn("uploads/anonymous/uuid-1");
        when(s3.presignedUrl(eq("uploads/anonymous/uuid-1"), any(Duration.class)))
                .thenReturn(new URL("https://example.com/presigned"));

        mvc.perform(multipart("/api/media/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.key").value("uploads/anonymous/uuid-1"))
                .andExpect(jsonPath("$.url").value("https://example.com/presigned"));

        verify(s3).upload(eq("uploads/anonymous"), any());
        verify(s3).presignedUrl(eq("uploads/anonymous/uuid-1"), any(Duration.class));
        verifyNoMoreInteractions(s3);
    }

    @Test
    void upload_authenticated_usesUsernamePrefix() throws Exception {
        var file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "bytes".getBytes());

        when(s3.upload(eq("uploads/alex"), any()))
                .thenReturn("uploads/alex/uuid-2");
        when(s3.presignedUrl(eq("uploads/alex/uuid-2"), any(Duration.class)))
                .thenReturn(new URL("https://example.com/alex"));

        mvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .with(user("alex"))) // simulate authenticated user
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("uploads/alex/uuid-2"))
                .andExpect(jsonPath("$.url").value("https://example.com/alex"));

        verify(s3).upload(eq("uploads/alex"), any());
        verify(s3).presignedUrl(eq("uploads/alex/uuid-2"), any(Duration.class));
        verifyNoMoreInteractions(s3);
    }
}