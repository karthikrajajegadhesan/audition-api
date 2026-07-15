package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
@Getter
public class AuditionIntegrationClient {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String POSTS_URL = BASE_URL + "/posts";
    private static final String POST_BY_ID_URL = POSTS_URL + "/{id}";
    private static final String POST_COMMENTS_URL = POSTS_URL + "/{postId}/comments";
    private static final String COMMENTS_BY_POST_ID_URL = BASE_URL + "/comments?postId={postId}";

    private final RestTemplate restTemplate;

    public AuditionIntegrationClient(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<AuditionPost> getPosts() {
        try {
            final AuditionPost[] posts = restTemplate.getForObject(POSTS_URL, AuditionPost[].class);
            return posts == null ? new ArrayList<>() : Arrays.asList(posts);
        } catch (final RestClientException exception) {
            throw handleRestClientException("getPosts", exception, null);
        }
    }

    public AuditionPost getPostById(final String id) {
        try {
            return restTemplate.getForObject(POST_BY_ID_URL, AuditionPost.class, id);
        } catch (final RestClientException exception) {
            throw handleRestClientException("getPostById", exception, id);
        }
    }

    public AuditionPost getPostWithComments(final String postId) {
        final AuditionPost post = getPostById(postId);
        try {
            final AuditionComment[] comments = restTemplate.getForObject(
                POST_COMMENTS_URL, AuditionComment[].class, postId);
            post.setComments(comments == null ? new ArrayList<>() : Arrays.asList(comments));
            return post;
        } catch (final RestClientException exception) {
            throw handleRestClientException("getPostWithComments", exception, postId);
        }
    }

    public List<AuditionComment> getCommentsByPostId(final String postId) {
        try {
            final AuditionComment[] comments = restTemplate.getForObject(
                COMMENTS_BY_POST_ID_URL, AuditionComment[].class, postId);
            return comments == null ? new ArrayList<>() : Arrays.asList(comments);
        } catch (final RestClientException exception) {
            throw handleRestClientException("getCommentsByPostId", exception, postId);
        }
    }

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
