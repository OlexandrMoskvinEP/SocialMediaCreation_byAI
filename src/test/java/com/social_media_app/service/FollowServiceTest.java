package com.social_media_app.service;

import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Follow;
import com.social_media_app.model.User;
import com.social_media_app.repository.FollowRepository;
import com.social_media_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = user(1L, "alice");
        bob = user(2L, "bob");
    }

    @Test
    void follow_success_whenNotFollowingYet() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(followRepository.existsByFollowerAndFollowed(alice, bob)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenAnswer(inv -> {
            Follow f = inv.getArgument(0);
            f.setId(10L);
            return f;
        });

        Follow saved = followService.follow(1L, 2L);

        assertThat(saved.getId()).isEqualTo(10L);
        assertThat(saved.getFollower()).isEqualTo(alice);
        assertThat(saved.getFollowed()).isEqualTo(bob);

        // verify payload passed to save
        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(captor.capture());
        Follow toSave = captor.getValue();
        assertThat(toSave.getFollower()).isEqualTo(alice);
        assertThat(toSave.getFollowed()).isEqualTo(bob);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(followRepository, times(1)).existsByFollowerAndFollowed(alice, bob);
    }

    @Test
    void follow_throwsConflict_whenSelfFollow() {
        assertThatThrownBy(() -> followService.follow(1L, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cannot follow yourself");
        verifyNoInteractions(userRepository, followRepository);
    }

    @Test
    void follow_throwsConflict_whenAlreadyFollowing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(followRepository.existsByFollowerAndFollowed(alice, bob)).thenReturn(true);

        assertThatThrownBy(() -> followService.follow(1L, 2L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Already following");

        verify(followRepository, never()).save(any());
    }

    @Test
    void follow_throwsNotFound_whenFollowerMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> followService.follow(1L, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Follower user not found");
        verify(userRepository, never()).findById(2L); // short‑circuits after missing follower
    }

    @Test
    void follow_throwsNotFound_whenFollowedMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.follow(1L, 2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Followed user not found");
    }

    // --- unfollow ---

    @Test
    void unfollow_deletes_whenRelationExists() {
        Follow relation = Follow.builder().id(42L).follower(alice).followed(bob).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(followRepository.findByFollowerAndFollowed(alice, bob))
                .thenReturn(Optional.of(relation));

        followService.unfollow(1L, 2L);

        verify(followRepository).delete(relation);
    }

    @Test
    void unfollow_noop_whenRelationMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
        when(followRepository.findByFollowerAndFollowed(alice, bob))
                .thenReturn(Optional.empty());

        followService.unfollow(1L, 2L);

        verify(followRepository, never()).delete(any());
    }

    @Test
    void unfollow_throwsNotFound_whenAnyUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> followService.unfollow(1L, 2L))
                .isInstanceOf(NotFoundException.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> followService.unfollow(1L, 2L))
                .isInstanceOf(NotFoundException.class);
    }

    // --- listings ---

    @Test
    void listFollowing_returnsRelations() {
        Follow r1 = Follow.builder().id(1L).follower(alice).followed(bob).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(followRepository.findAllByFollower(alice)).thenReturn(List.of(r1));

        List<Follow> result = followService.listFollowing(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFollowed().getUsername()).isEqualTo("bob");
    }

    @Test
    void listFollowers_returnsRelations() {
        Follow r1 = Follow.builder().id(2L).follower(bob).followed(alice).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(followRepository.findAllByFollowed(alice)).thenReturn(List.of(r1));

        List<Follow> result = followService.listFollowers(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFollower().getUsername()).isEqualTo("bob");
    }

    @Test
    void listFollowing_throwsNotFound_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> followService.listFollowing(99L))
                .isInstanceOf(NotFoundException.class);
        verify(followRepository, never()).findAllByFollower(any());
    }

    @Test
    void listFollowers_throwsNotFound_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> followService.listFollowers(99L))
                .isInstanceOf(NotFoundException.class);
        verify(followRepository, never()).findAllByFollowed(any());
    }

    private static User user(Long id, String username) {
        return User.builder().id(id).username(username).build();
    }
}
