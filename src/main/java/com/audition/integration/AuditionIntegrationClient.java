package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for JSONPlaceholder. Translates RestClientException failures into
 * SystemException instances with appropriate status codes for the API layer.
 */
@Component
@Getter
public class AuditionIntegrationClient {

    private final RestTemplate restTemplate;
    private final String postsUrl;
    private final String postByIdUrl;
    private final String postCommentsUrl;
    private final String commentsByPostIdUrl;

    public AuditionIntegrationClient(final RestTemplate restTemplate,
        @Value("${application.integration.posts-url}") final String postsUrl,
        @Value("${application.integration.post-by-id-url}") final String postByIdUrl,
        @Value("${application.integration.post-comments-url}") final String postCommentsUrl,
        @Value("${application.integration.comments-by-post-id-url}") final String commentsByPostIdUrl) {
        this.restTemplate = restTemplate;
        this.postsUrl = postsUrl;
        this.postByIdUrl = postByIdUrl;
        this.postCommentsUrl = postCommentsUrl;
        this.commentsByPostIdUrl = commentsByPostIdUrl;
    }

    /** Fetches all posts from the downstream service. */
    public List<AuditionPost> getPosts() {
        try {
            final AuditionPost[] posts = restTemplate.getForObject(postsUrl, AuditionPost[].class);
            return posts == null ? new ArrayList<>() : Arrays.asList(posts);
        } catch (final RestClientException exception) {
            throw handleRestClientException("getPosts", exception, null);
        }
    }

    /** Fetches a single post by post id. */
    public AuditionPost getPostById(final String id) {
        try {
            return restTemplate.getForObject(postByIdUrl, AuditionPost.class, id);
        } catch (final RestClientException exception) {
            throw handleRestClientException("getPostById", exception, id);
        }
    }

    /** Fetches a post and enriches it with comments from a second upstream call. */
    public AuditionPost getPostWithComments(final String postId) {
        final AuditionPost post = getPostById(postId);
        try {
            final AuditionComment[] comments = restTemplate.getForObject(
                postCommentsUrl, AuditionComment[].class, postId);
            post.setComments(comments == null ? new ArrayList<>() : Arrays.asList(comments));
            return post;
        } catch (final RestClientException exception) {
            throw handleRestClientException("getPostWithComments", exception, postId);
        }
    }

    /** Fetches comments for a post using the upstream postId query parameter. */
    public List<AuditionComment> getCommentsByPostId(final String postId) {
        try {
            final AuditionComment[] comments = restTemplate.getForObject(
                commentsByPostIdUrl, AuditionComment[].class, postId);
            return comments == null ? new ArrayList<>() : Arrays.asList(comments);
        } catch (final RestClientException exception) {
            throw handleRestClientException("getCommentsByPostId", exception, postId);
        }
    }

    /**
     * Converts RestTemplate failures into domain exceptions. A 404 with a resource id is mapped
     * to a user-friendly "post not found" message.
     */
    private SystemException handleRestClientException(final String operation,
        final RestClientException exception, final String resourceId) {
        if (exception instanceof HttpClientErrorException clientError) {
            if (clientError.getStatusCode() == HttpStatus.NOT_FOUND && resourceId != null) {
                return new SystemException("Cannot find a Post with id " + resourceId,
                    "Resource Not Found", 404, clientError);
            }
            return new SystemException(getErrorMessage(clientError), "Client Error",
                clientError.getStatusCode().value(), clientError);
        }

        if (exception instanceof HttpServerErrorException serverError) {
            return new SystemException(getErrorMessage(serverError), "Upstream Service Error",
                serverError.getStatusCode().value(), serverError);
        }

        if (exception instanceof ResourceAccessException) {
            return new SystemException("Unable to reach remote service", "Service Unavailable", 503,
                exception);
        }

        return new SystemException(
            String.format("Unexpected error during %s", operation), "Integration Error", 500,
            exception);
    }

    private String getErrorMessage(final RestClientResponseException exception) {
        final String responseBody = exception.getResponseBodyAsString();
        if (responseBody != null && !responseBody.isBlank()) {
            return responseBody;
        }
        return exception.getMessage();
    }
}
