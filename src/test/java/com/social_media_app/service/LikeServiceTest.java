package com.social_media_app.service;

import com.social_media_app.exceptions.ConflictException;
import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Post;
import com.social_media_app.model.PostLike;
import com.social_media_app.model.User;
import com.social_media_app.repository.PostLikeRepository;
import com.social_media_app.repository.PostRepository;
import com.social_media_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private PostLikeRepository likeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private LikeService likeService;

    private User alice;
    private Post post1;

    @BeforeEach
    void setUp() {
        alice = User.builder().id(1L).username("alice").build();
        post1 = Post.builder().id(100L).title("Hello").body("Body").author(alice).build();
    }

    // --- like ---

    @Test
    void like_success_whenNotLikedYet() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post1));
        when(likeRepository.existsByUserAndPost(alice, post1)).thenReturn(false);
        when(likeRepository.save(any(PostLike.class))).thenAnswer(inv -> {
            PostLike pl = inv.getArgument(0);
            pl.setId(10L);
            return pl;
        });

        likeService.like(1L, 100L);

        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(likeRepository).save(captor.capture());
        PostLike saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(alice);
        assertThat(saved.getPost()).isEqualTo(post1);

        verify(likeRepository, times(1)).existsByUserAndPost(alice, post1);
    }

    @Test
    void like_throwsConflict_whenAlreadyLiked() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post1));
        when(likeRepository.existsByUserAndPost(alice, post1)).thenReturn(true);

        assertThatThrownBy(() -> likeService.like(1L, 100L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Already liked");

        verify(likeRepository, never()).save(any());
    }

    @Test
    void like_throwsNotFound_whenUserMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(1L, 100L))
                .isInstanceOf(NotFoundException.class);

        verify(postRepository, never()).findById(anyLong());
        verify(likeRepository, never()).existsByUserAndPost(any(), any());
    }

    @Test
    void like_throwsNotFound_whenPostMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(1L, 100L))
                .isInstanceOf(NotFoundException.class);

        verify(likeRepository, never()).existsByUserAndPost(any(), any());
    }

    // --- unlike ---

    @Test
    void unlike_deletes_whenExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post1));

        likeService.unlike(1L, 100L);

        verify(likeRepository).deleteByUserAndPost(alice, post1);
    }

    @Test
    void unlike_throwsNotFound_whenUserOrPostMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> likeService.unlike(1L, 100L))
                .isInstanceOf(NotFoundException.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> likeService.unlike(1L, 100L))
                .isInstanceOf(NotFoundException.class);
    }

    // --- count & check ---

    @Test
    void countLikes_returnsValue() {
        when(postRepository.findById(100L)).thenReturn(Optional.of(post1));
        when(likeRepository.countByPost(post1)).thenReturn(3L);

        long count = likeService.countLikes(100L);
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void countLikes_throwsNotFound_whenPostMissing() {
        when(postRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> likeService.countLikes(100L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void isLiked_returnsTrueOrFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findById(100L)).thenReturn(Optional.of(post1));

        when(likeRepository.existsByUserAndPost(alice, post1)).thenReturn(true);
        assertThat(likeService.isLiked(1L, 100L)).isTrue();

        when(likeRepository.existsByUserAndPost(alice, post1)).thenReturn(false);
        assertThat(likeService.isLiked(1L, 100L)).isFalse();
    }

    @Test
    void isLiked_throwsNotFound_whenUserOrPostMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> likeService.isLiked(1L, 100L))
                .isInstanceOf(NotFoundException.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> likeService.isLiked(1L, 100L))
                .isInstanceOf(NotFoundException.class);
    }
}
