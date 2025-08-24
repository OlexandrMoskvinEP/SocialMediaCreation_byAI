package com.social_media_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_media_app.config.SecurityConfig;
import com.social_media_app.exceptions.GlobalExceptionHandler;
import com.social_media_app.model.dto.RegisterRequest;
import com.social_media_app.model.dto.UserResponse;
import com.social_media_app.security.JwtFilter;
import com.social_media_app.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    AuthService authService;
    @MockitoBean
    JwtFilter jwtFilter;

    @BeforeEach
    void letFilterPass() throws Exception {
        doAnswer(inv -> {
            var req   = (jakarta.servlet.ServletRequest)  inv.getArgument(0);
            var res   = (jakarta.servlet.ServletResponse) inv.getArgument(1);
            var chain = (jakarta.servlet.FilterChain)     inv.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void register_success_returns201_andBody() throws Exception {
        var req = new RegisterRequest("alex", "a@a.com", "secret123");
        var resp = new UserResponse(1L, "alex", "a@a.com");

        when(authService.register(any())).thenReturn(resp);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alex"))
                .andExpect(jsonPath("$.email").value("a@a.com"));

        verify(authService, times(1)).register(any());
        verifyNoMoreInteractions(authService);
    }

    @Test
    void register_validationError_returns400_andDoesNotCallService() throws Exception {
        var badBody = Map.of("username", "", "email", "not-an-email", "password", "123");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badBody)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void register_usernameTaken_mappedTo409() throws Exception {
        var req = new RegisterRequest("alex", "a@a.com", "secret123");

        when(authService.register(any()))
                .thenThrow(new IllegalArgumentException("username taken"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());

        verify(authService, times(1)).register(any());
    }
}