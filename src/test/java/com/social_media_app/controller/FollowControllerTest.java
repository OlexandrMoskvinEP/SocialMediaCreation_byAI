package com.social_media_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_media_app.config.SecurityConfig;
import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.GlobalExceptionHandler;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Follow;
import com.social_media_app.model.User;
import com.social_media_app.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FollowController.class)
@WithMockUser(username = "testuser")
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class FollowControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private FollowService followService;

    @Test
    void follow_returns201_withPayload() throws Exception {
        User alice = User.builder().id(1L).username("alice").build();
        User bob = User.builder().id(2L).username("bob").build();
        Follow saved = Follow.builder().id(10L).follower(alice).followed(bob).build();

        when(followService.follow(1L, 2L)).thenReturn(saved);

        String body = om.writeValueAsString(new FollowController.FollowRequest(1L, 2L));

        mvc.perform(post("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.followerId", is(1)))
                .andExpect(jsonPath("$.followedId", is(2)));
    }

    @Test
    void follow_conflict_returns409() throws Exception {
        when(followService.follow(1L, 1L))
                .thenThrow(new ConflictException("You cannot follow yourself"));

        String body = om.writeValueAsString(new FollowController.FollowRequest(1L, 1L));

        mvc.perform(post("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail", is("You cannot follow yourself")));
    }

    @Test
    void follow_notFound_returns404() throws Exception {
        when(followService.follow(1L, 99L))
                .thenThrow(new NotFoundException("Followed user not found: id=99"));

        String body = om.writeValueAsString(new FollowController.FollowRequest(1L, 99L));

        mvc.perform(post("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("Followed user not found: id=99")));
    }

    @Test
    void unfollow_returns204() throws Exception {
        String body = om.writeValueAsString(new FollowController.FollowRequest(1L, 2L));

        mvc.perform(delete("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    @Test
    void unfollow_notFound_returns404() throws Exception {
        doThrow(new NotFoundException("Follower user not found: id=7")).when(followService).unfollow(7L, 9L);
        String body = om.writeValueAsString(new FollowController.FollowRequest(7L, 9L));

        mvc.perform(delete("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("Follower user not found: id=7")));
    }

    @Test
    void listFollowing_returnsArray() throws Exception {
        User alice = User.builder().id(1L).build();
        User bob = User.builder().id(2L).build();
        Follow r1 = Follow.builder().id(100L).follower(alice).followed(bob).build();

        when(followService.listFollowing(1L)).thenReturn(List.of(r1));

        mvc.perform(get("/api/follows/following/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].followerId", is(1)))
                .andExpect(jsonPath("$[0].followedId", is(2)));
    }

    @Test
    void listFollowers_returnsArray() throws Exception {
        User alice = User.builder().id(1L).build();
        User bob = User.builder().id(2L).build();
        Follow r1 = Follow.builder().id(200L).follower(bob).followed(alice).build();

        when(followService.listFollowers(1L)).thenReturn(List.of(r1));

        mvc.perform(get("/api/follows/followers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(200)))
                .andExpect(jsonPath("$[0].followerId", is(2)))
                .andExpect(jsonPath("$[0].followedId", is(1)));
    }

    @Test
    void lists_404_whenUserMissing() throws Exception {
        when(followService.listFollowing(anyLong()))
                .thenThrow(new NotFoundException("User not found: id=99"));

        mvc.perform(get("/api/follows/following/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("User not found: id=99")));

        when(followService.listFollowers(anyLong()))
                .thenThrow(new NotFoundException("User not found: id=77"));

        mvc.perform(get("/api/follows/followers/77"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("User not found: id=77")));
    }
}
