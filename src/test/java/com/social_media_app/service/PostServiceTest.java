package com.social_media_app.service;

import com.social_media_app.exceptions.NotFoundException;
import com.social_media_app.model.Follow;
import com.social_media_app.model.Post;
import com.social_media_app.model.User;
import com.social_media_app.repository.FollowRepository;
import com.social_media_app.repository.PostRepository;
import com.social_media_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private PostService postService;

    private User alice; // id=1
    private User bob;   // id=2
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        alice = User.builder().id(1L).username("alice").build();
        bob = User.builder().id(2L).username("bob").build();
        pageable = PageRequest.of(0, 10);
    }

    // --- createPost ---

    @Test
    void createPost_success_whenAuthorExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        Post created = postService.createPost(1L, "Hello", "Body");

        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getAuthor()).isEqualTo(alice);
        assertThat(created.getTitle()).isEqualTo("Hello");
        assertThat(created.getBody()).isEqualTo("Body");

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post toSave = captor.getValue();
        assertThat(toSave.getAuthor()).isEqualTo(alice);
        assertThat(toSave.getTitle()).isEqualTo("Hello");
        assertThat(toSave.getBody()).isEqualTo("Body");
    }

    @Test
    void createPost_throwsNotFound_whenAuthorMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.createPost(99L, "T", "B"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Author not found");
        verify(postRepository, never()).save(any());
    }

    // --- getById ---

    @Test
    void getById_returnsPost_whenExists() {
        Post p = Post.builder().id(5L).author(alice).title("t").body("b").build();
        when(postRepository.findById(5L)).thenReturn(Optional.of(p));

        Post got = postService.getById(5L);

        assertThat(got.getId()).isEqualTo(5L);
        assertThat(got.getAuthor()).isEqualTo(alice);
    }

    @Test
    void getById_throwsNotFound_whenMissing() {
        when(postRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getById(404L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Post not found");
    }

    // --- listByAuthor ---

    @Test
    void listByAuthor_returnsPage_whenAuthorExists() {
        Post p1 = Post.builder().id(1L).author(alice).title("A1").body("...").build();
        Post p2 = Post.builder().id(2L).author(alice).title("A2").body("...").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(postRepository.findAllByAuthorOrderByCreatedAtDesc(alice, pageable))
                .thenReturn(new PageImpl<>(List.of(p1, p2), pageable, 2));

        Page<Post> page = postService.listByAuthor(1L, pageable);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Post::getAuthor).containsOnly(alice);
    }

    @Test
    void listByAuthor_throwsNotFound_whenAuthorMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.listByAuthor(1L, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Author not found");
        verify(postRepository, never()).findAllByAuthorOrderByCreatedAtDesc(any(), any());
    }

    // --- feedFor ---

    @Test
    void feedFor_returnsEmpty_whenUserFollowsNobody() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(followRepository.findAllByFollower(alice)).thenReturn(List.of());

        Page<Post> page = postService.feedFor(1L, pageable);

        assertThat(page.getTotalElements()).isZero();
        assertThat(page.getContent()).isEmpty();
        verify(postRepository, never()).findAllByAuthorIdInOrderByCreatedAtDesc(anyList(), any());
    }

    @Test
    void feedFor_returnsPosts_fromFollowedAuthors() {
        // alice follows bob
        Follow f = Follow.builder().id(10L).follower(alice).followed(bob).build();
        Post b1 = Post.builder().id(11L).author(bob).title("B1").body("..").build();
        Post b2 = Post.builder().id(12L).author(bob).title("B2").body("..").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(followRepository.findAllByFollower(alice)).thenReturn(List.of(f));
        when(postRepository.findAllByAuthorIdInOrderByCreatedAtDesc(List.of(2L), pageable))
                .thenReturn(new PageImpl<>(List.of(b1, b2), pageable, 2));

        Page<Post> feed = postService.feedFor(1L, pageable);

        assertThat(feed.getTotalElements()).isEqualTo(2);
        assertThat(feed.getContent()).extracting(Post::getAuthor).containsOnly(bob);
    }

    @Test
    void feedFor_throwsNotFound_whenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.feedFor(99L, pageable))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
        verify(followRepository, never()).findAllByFollower(any());
        verify(postRepository, never()).findAllByAuthorIdInOrderByCreatedAtDesc(anyList(), any());
    }
}
