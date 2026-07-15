package com.audition.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AuditionIntegrationClientTest {

    private static final String POSTS_URL = "https://jsonplaceholder.typicode.com/posts";
    private static final String POST_BY_ID_URL = POSTS_URL + "/{id}";
    private static final String POST_COMMENTS_URL = POSTS_URL + "/{postId}/comments";
    private static final String COMMENTS_BY_POST_ID_URL =
        "https://jsonplaceholder.typicode.com/comments?postId={postId}";

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuditionIntegrationClient auditionIntegrationClient;

    @Test
    void getPosts_returnsPostsFromRestTemplate() {
        final AuditionPost[] posts = {createPost(1, 1, "sunt aut", "body")};
        when(restTemplate.getForObject(POSTS_URL, AuditionPost[].class)).thenReturn(posts);

        final List<AuditionPost> result = auditionIntegrationClient.getPosts();

        assertEquals(1, result.size());
        assertEquals("sunt aut", result.get(0).getTitle());
        verify(restTemplate).getForObject(POSTS_URL, AuditionPost[].class);
    }

    @Test
    void getPosts_nullResponse_returnsEmptyList() {
        when(restTemplate.getForObject(POSTS_URL, AuditionPost[].class)).thenReturn(null);

        final List<AuditionPost> result = auditionIntegrationClient.getPosts();

        assertTrue(result.isEmpty());
    }

    @Test
    void getPostById_returnsPostFromRestTemplate() {
        final AuditionPost post = createPost(1, 1, "title", "body");
        when(restTemplate.getForObject(POST_BY_ID_URL, AuditionPost.class, "1")).thenReturn(post);

        final AuditionPost result = auditionIntegrationClient.getPostById("1");

        assertEquals(1, result.getId());
        verify(restTemplate).getForObject(POST_BY_ID_URL, AuditionPost.class, "1");
    }

    @Test
    void getPostById_notFound_throwsSystemException() {
        when(restTemplate.getForObject(POST_BY_ID_URL, AuditionPost.class, "99"))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPostById("99"));

        assertEquals(404, exception.getStatusCode());
        assertEquals("Resource Not Found", exception.getTitle());
        assertEquals("Cannot find a Post with id 99", exception.getMessage());
    }

    @Test
    void getPostById_otherClientError_throwsSystemException() {
        when(restTemplate.getForObject(POST_BY_ID_URL, AuditionPost.class, "1"))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getPostById("1"));
    }

    @Test
    void getPostWithComments_returnsPostWithComments() {
        final AuditionPost post = createPost(1, 1, "title", "body");
        final AuditionComment[] comments = {createComment(1, 1, "comment")};
        when(restTemplate.getForObject(POST_BY_ID_URL, AuditionPost.class, "1")).thenReturn(post);
        when(restTemplate.getForObject(POST_COMMENTS_URL, AuditionComment[].class, "1"))
            .thenReturn(comments);

        final AuditionPost result = auditionIntegrationClient.getPostWithComments("1");

        assertEquals(1, result.getComments().size());
        assertEquals("comment", result.getComments().get(0).getBody());
    }

    @Test
    void getPostWithComments_nullComments_returnsPostWithEmptyComments() {
        final AuditionPost post = createPost(1, 1, "title", "body");
        when(restTemplate.getForObject(POST_BY_ID_URL, AuditionPost.class, "1")).thenReturn(post);
        when(restTemplate.getForObject(POST_COMMENTS_URL, AuditionComment[].class, "1"))
            .thenReturn(null);

        final AuditionPost result = auditionIntegrationClient.getPostWithComments("1");

        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void getCommentsByPostId_returnsCommentsFromRestTemplate() {
        final AuditionComment[] comments = {createComment(1, 1, "comment body")};
        when(restTemplate.getForObject(COMMENTS_BY_POST_ID_URL, AuditionComment[].class, "1"))
            .thenReturn(comments);

        final List<AuditionComment> result = auditionIntegrationClient.getCommentsByPostId("1");

        assertEquals(1, result.size());
        assertEquals("comment body", result.get(0).getBody());
        verify(restTemplate).getForObject(COMMENTS_BY_POST_ID_URL, AuditionComment[].class, "1");
    }

    @Test
    void getCommentsByPostId_nullResponse_returnsEmptyList() {
        when(restTemplate.getForObject(COMMENTS_BY_POST_ID_URL, AuditionComment[].class, "1"))
            .thenReturn(null);

        final List<AuditionComment> result = auditionIntegrationClient.getCommentsByPostId("1");

        assertTrue(result.isEmpty());
    }

    @Test
    void getPosts_connectionError_throwsSystemException() {
        when(restTemplate.getForObject(POSTS_URL, AuditionPost[].class))
            .thenThrow(new ResourceAccessException("Connection refused"));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getPosts());

        assertEquals(503, exception.getStatusCode());
        assertEquals("Service Unavailable", exception.getTitle());
    }

    @Test
    void getCommentsByPostId_serverError_throwsSystemException() {
        when(restTemplate.getForObject(COMMENTS_BY_POST_ID_URL, AuditionComment[].class, "1"))
            .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        final SystemException exception = assertThrows(SystemException.class,
            () -> auditionIntegrationClient.getCommentsByPostId("1"));

        assertEquals(502, exception.getStatusCode());
        assertEquals("Upstream Service Error", exception.getTitle());
    }

    private AuditionPost createPost(final int id, final int userId, final String title,
        final String body) {
        final AuditionPost post = new AuditionPost();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody(body);
        return post;
    }

    private AuditionComment createComment(final int id, final int postId, final String body) {
        final AuditionComment comment = new AuditionComment();
        comment.setId(id);
        comment.setPostId(postId);
        comment.setBody(body);
        return comment;
    }

}
