package com.social_media_app.service;

import com.social_media_app.model.User;
import com.social_media_app.model.dto.RegisterRequest;
import com.social_media_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository users;
    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    AuthService service;

    @Test
    void register_success() {
        var req = new RegisterRequest("alex", "A@A.com", "secret123");

        when(users.existsByUsername("alex")).thenReturn(false);
        when(users.existsByEmail("a@a.com")).thenReturn(false);
        when(encoder.encode("secret123")).thenReturn("ENCODED");
        var saved = User.builder()
                .id(42L).username("alex").email("a@a.com")
                .passwordHash("ENCODED").createdAt(Instant.now()).active(true)
                .build();
        // capture the first save argument and return "saved"
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(users.save(captor.capture())).thenReturn(saved);

        var resp = service.register(req);

        assertEquals(42L, resp.id());
        assertEquals("alex", resp.username());
        assertEquals("a@a.com", resp.email()); // lowercased
        User toSave = captor.getValue();
        assertEquals("alex", toSave.getUsername());
        assertEquals("a@a.com", toSave.getEmail());
        assertEquals("ENCODED", toSave.getPasswordHash());
        verify(users, times(1)).save(any(User.class));
        verifyNoMoreInteractions(users);
    }

    @Test
    void register_usernameTaken() {
        var req = new RegisterRequest("alex", "a@a.com", "x");
        when(users.existsByUsername("alex")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.register(req));
        verify(users, never()).save(any());
    }

    @Test
    void register_emailTaken() {
        var req = new RegisterRequest("alex", "a@a.com", "x");
        when(users.existsByUsername("alex")).thenReturn(false);
        when(users.existsByEmail("a@a.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.register(req));
        verify(users, never()).save(any());
    }
}
