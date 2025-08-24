package com.social_media_app.service;

import com.social_media_app.model.User;
import com.social_media_app.repository.UserRepository;
import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional
    public User createUser(String username) {
        if (users.existsByUsername(username)) {
            throw new ConflictException("Username already taken: " + username);
        }
        return users.save(User.builder().username(username).build());
    }

    public User getById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: id=" + id));
    }

    public User getByUsername(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }
}
