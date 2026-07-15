package com.audition.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditionServiceTest {

    @Mock
    private AuditionIntegrationClient auditionIntegrationClient;

    @InjectMocks
    private AuditionService auditionService;

    @Test
    void getPosts_returnsAllPostsWhenNoFiltersApplied() {
        when(auditionIntegrationClient.getPosts()).thenReturn(List.of(
            createPost(1, 1, "title one"),
            createPost(2, 2, "title two")));

        final List<AuditionPost> result = auditionService.getPosts(null, null);

        assertEquals(2, result.size());
        verify(auditionIntegrationClient).getPosts();
    }

    @Test
    void getPosts_filtersByUserId() {
        when(auditionIntegrationClient.getPosts()).thenReturn(List.of(
            createPost(1, 1, "title one"),
            createPost(2, 2, "title two")));

        final List<AuditionPost> result = auditionService.getPosts(1, null);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getUserId());
    }

    @Test
    void getPosts_filtersByTitleCaseInsensitively() {
        when(auditionIntegrationClient.getPosts()).thenReturn(List.of(
            createPost(1, 1, "SUNT aut facere"),
            createPost(2, 2, "other title")));

        final List<AuditionPost> result = auditionService.getPosts(null, "sunt");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void getPosts_filtersByUserIdAndTitle() {
        when(auditionIntegrationClient.getPosts()).thenReturn(List.of(
            createPost(1, 1, "matching title"),
            createPost(2, 1, "other title")));

        final List<AuditionPost> result = auditionService.getPosts(1, "matching");

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
    }

    @Test
    void getPostById_delegatesToIntegrationClient() {
        final AuditionPost post = createPost(1, 1, "title");
        when(auditionIntegrationClient.getPostById("1")).thenReturn(post);

        final AuditionPost result = auditionService.getPostById("1");

        assertEquals(post, result);
        verify(auditionIntegrationClient).getPostById("1");
    }

    @Test
    void getPostWithComments_delegatesToIntegrationClient() {
        final AuditionPost post = createPost(1, 1, "title");
        when(auditionIntegrationClient.getPostWithComments("1")).thenReturn(post);

        final AuditionPost result = auditionService.getPostWithComments("1");

        assertEquals(post, result);
        verify(auditionIntegrationClient).getPostWithComments("1");
    }

    @Test
    void getCommentsByPostId_delegatesToIntegrationClient() {
        final List<AuditionComment> comments = List.of(createComment(1));
        when(auditionIntegrationClient.getCommentsByPostId("1")).thenReturn(comments);

        final List<AuditionComment> result = auditionService.getCommentsByPostId("1");

        assertEquals(comments, result);
        verify(auditionIntegrationClient).getCommentsByPostId("1");
    }

    private AuditionPost createPost(final int id, final int userId, final String title) {
        final AuditionPost post = new AuditionPost();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody("body");
        return post;
    }

    private AuditionComment createComment(final int id) {
        final AuditionComment comment = new AuditionComment();
        comment.setId(id);
        comment.setPostId(1);
        comment.setBody("comment");
        return comment;
    }

}
