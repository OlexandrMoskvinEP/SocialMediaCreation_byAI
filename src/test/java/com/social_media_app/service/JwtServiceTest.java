package com.social_media_app.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generate_and_parse_token_work_correctly() {
        String secret = "my-super-secret-key-which-is-long-enough";
        long ttlMin = 15;
        JwtService jwtService = new JwtService(secret, ttlMin);

        String token = jwtService.generate("alice");
        String subject = jwtService.getSubject(token);

        assertThat(subject).isEqualTo("alice");
    }


}