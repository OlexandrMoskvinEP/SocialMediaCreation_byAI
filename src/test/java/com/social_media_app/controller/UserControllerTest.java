package com.social_media_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.GlobalExceptionHandler;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.User;
import com.social_media_app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;
    @MockitoBean
    private UserService userService;

    @Test
    void create_returns201_andBody() throws Exception {
        User created = User.builder().id(10L).username("alice").build();
        when(userService.createUser("alice")).thenReturn(created);

        String body = om.writeValueAsString(new UserController.CreateUserRequest("alice"));

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.username", is("alice")));

        verify(userService).createUser("alice");
    }

    @Test
    void create_conflict_returns409() throws Exception {
        when(userService.createUser("alice")).thenThrow(new ConflictException("Username already taken"));

        String body = om.writeValueAsString(new UserController.CreateUserRequest("alice"));

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("detail", is("Username already taken"))); // ProblemDetail

        verify(userService).createUser("alice");
    }

    @Test
    void getById_ok() throws Exception {
        User u = User.builder().id(5L).username("bob").build();

        when(userService.getById(5L)).thenReturn(u);

        mvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.username", is("bob")));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userService.getById(404L)).thenThrow(new NotFoundException("User not found: id=404"));

        mvc.perform(get("/api/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("detail", is("User not found: id=404")));
    }

    @Test
    void getByUsername_ok() throws Exception {
        User u = User.builder().id(7L).username("carol").build();

        when(userService.getByUsername("carol")).thenReturn(u);

        mvc.perform(get("/api/users/by-username/carol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(7)))
                .andExpect(jsonPath("$.username", is("carol")));
    }
}
