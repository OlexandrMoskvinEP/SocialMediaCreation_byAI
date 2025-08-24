package com.social_media_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_media_app.config.SecurityConfig;
import com.social_media_app.exceptions.GlobalExceptionHandler;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Post;
import com.social_media_app.model.User;
import com.social_media_app.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@WithMockUser(username = "testuser")
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class PostControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;
    @MockitoBean
    private PostService postService;

    @Test
    void create_returns201_andBody() throws Exception {
        Post p = Post.builder()
                .id(100L)
                .author(User.builder().id(1L).username("alice").build())
                .title("Hello")
                .body("World")
                .build();

        when(postService.createPost(1L, "Hello", "World")).thenReturn(p);

        String body = om.writeValueAsString(
                new PostController.CreatePostRequest(1L, "Hello", "World")
        );

        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.authorId", is(1)))
                .andExpect(jsonPath("$.title", is("Hello")))
                .andExpect(jsonPath("$.body", is("World")));
    }

    @Test
    void create_returns404_whenAuthorMissing() throws Exception {
        when(postService.createPost(99L, "T", "B"))
                .thenThrow(new NotFoundException("Author not found: id=99"));

        String body = om.writeValueAsString(
                new PostController.CreatePostRequest(99L, "T", "B")
        );

        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("Author not found: id=99")));
    }

    @Test
    void getById_ok() throws Exception {
        Post p = Post.builder()
                .id(5L)
                .author(User.builder().id(1L).username("alice").build())
                .title("T")
                .body("B")
                .build();

        when(postService.getById(5L)).thenReturn(p);

        mvc.perform(get("/api/posts/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.authorId", is(1)))
                .andExpect(jsonPath("$.title", is("T")))
                .andExpect(jsonPath("$.body", is("B")));
    }

    @Test
    void getById_404() throws Exception {
        when(postService.getById(404L))
                .thenThrow(new NotFoundException("Post not found: id=404"));

        mvc.perform(get("/api/posts/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("Post not found: id=404")));
    }

    @Test
    void listByAuthor_ok_withPaging() throws Exception {
        Post p1 = Post.builder().id(1L).author(User.builder().id(1L).build()).title("A1").body("...").build();
        Post p2 = Post.builder().id(2L).author(User.builder().id(1L).build()).title("A2").body("...").build();

        when(postService.listByAuthor(1L, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 2), 2));

        mvc.perform(get("/api/posts/by-author/1?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].authorId", is(1)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void feed_ok_withPaging() throws Exception {
        Post p1 = Post.builder().id(11L).author(User.builder().id(2L).build()).title("B1").body("..").build();
        Post p2 = Post.builder().id(12L).author(User.builder().id(2L).build()).title("B2").body("..").build();

        when(postService.feedFor(1L, PageRequest.of(1, 2)))
                .thenReturn(new PageImpl<>(List.of(p1, p2), PageRequest.of(1, 2), 4));

        mvc.perform(get("/api/posts/feed/1?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].authorId", is(2)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(4)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    void feed_404_whenUserMissing() throws Exception {
        when(postService.feedFor(eq(99L), any()))
                .thenThrow(new NotFoundException("User not found: id=99"));

        mvc.perform(get("/api/posts/feed/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("User not found: id=99")));
    }
}
