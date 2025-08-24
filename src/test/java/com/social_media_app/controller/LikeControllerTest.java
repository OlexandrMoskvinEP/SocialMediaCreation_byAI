package com.social_media_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_media_app.config.SecurityConfig;
import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.GlobalExceptionHandler;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.service.LikeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeController.class)
@WithMockUser(username = "testuser")
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class LikeControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private LikeService likeService;

    @Test
    void like_returns204() throws Exception {
        doNothing().when(likeService).like(1L, 100L);

        String body = om.writeValueAsString(new LikeController.LikeRequest(1L, 100L));

        mvc.perform(post("/api/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void like_conflict_returns409() throws Exception {
        String msg = "Already liked";
        // Simulate conflict
        org.mockito.Mockito.doThrow(new ConflictException(msg))
                .when(likeService).like(1L, 100L);

        String body = om.writeValueAsString(new LikeController.LikeRequest(1L, 100L));

        mvc.perform(post("/api/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail", is(msg)));
    }

    @Test
    void like_notFound_returns404() throws Exception {
        String msg = "User not found: id=1";

        org.mockito.Mockito.doThrow(new NotFoundException(msg))
                .when(likeService).like(1L, 100L);

        String body = om.writeValueAsString(new LikeController.LikeRequest(1L, 100L));

        mvc.perform(post("/api/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is(msg)));
    }

    // --- unlike ---
    @Test
    void unlike_returns204() throws Exception {
        doNothing().when(likeService).unlike(1L, 100L);

        String body = om.writeValueAsString(new LikeController.LikeRequest(1L, 100L));

        mvc.perform(delete("/api/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void unlike_notFound_returns404() throws Exception {
        String msg = "Post not found: id=100";
        org.mockito.Mockito.doThrow(new NotFoundException(msg))
                .when(likeService).unlike(1L, 100L);

        String body = om.writeValueAsString(new LikeController.LikeRequest(1L, 100L));

        mvc.perform(delete("/api/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is(msg)));
    }

    // --- count ---
    @Test
    void count_returnsPayload() throws Exception {
        when(likeService.countLikes(100L)).thenReturn(3L);

        mvc.perform(get("/api/likes/count/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId", is(100)))
                .andExpect(jsonPath("$.count", is(3)));
    }

    @Test
    void count_notFound_returns404() throws Exception {
        when(likeService.countLikes(anyLong()))
                .thenThrow(new NotFoundException("Post not found: id=404"));

        mvc.perform(get("/api/likes/count/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("Post not found: id=404")));
    }

    // --- check ---
    @Test
    void check_returnsTrueOrFalse() throws Exception {
        when(likeService.isLiked(1L, 100L)).thenReturn(true);
        mvc.perform(get("/api/likes/check")
                        .param("userId", "1")
                        .param("postId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.postId", is(100)))
                .andExpect(jsonPath("$.liked", is(true)));

        when(likeService.isLiked(1L, 100L)).thenReturn(false);
        mvc.perform(get("/api/likes/check")
                        .param("userId", "1")
                        .param("postId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked", is(false)));
    }

    @Test
    void check_notFound_returns404() throws Exception {
        when(likeService.isLiked(1L, 100L))
                .thenThrow(new NotFoundException("User not found: id=1"));

        mvc.perform(get("/api/likes/check")
                        .param("userId", "1")
                        .param("postId", "100"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("User not found: id=1")));
    }
}
