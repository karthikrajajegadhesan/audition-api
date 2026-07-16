package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import org.springframework.stereotype.Service;

/**
 * Business logic for posts and comments. Fetches data from the integration layer and applies
 * in-memory filtering where the downstream API does not support query parameters.
 */
@Service
@Getter
public class AuditionService {

    private final AuditionIntegrationClient auditionIntegrationClient;

    public AuditionService(final AuditionIntegrationClient auditionIntegrationClient) {
        this.auditionIntegrationClient = auditionIntegrationClient;
    }

    /**
     * Returns posts filtered by optional user id and title case-insensitive title substring.
     */
    public List<AuditionPost> getPosts(final Integer userId, final String title) {
        return auditionIntegrationClient.getPosts().stream()
            .filter(post -> userId == null || post.getUserId() == userId)
            .filter(post -> title == null || post.getTitle() != null
                && post.getTitle().toLowerCase(Locale.ROOT).contains(title.toLowerCase(Locale.ROOT)))
            .toList();
    }

    /** Returns a single post by post id. */
    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    /** Returns a post with its comments. */
    public AuditionPost getPostWithComments(final String postId) {
        return auditionIntegrationClient.getPostWithComments(postId);
    }

    /** Returns comments for a post as a list. */
    public List<AuditionComment> getCommentsByPostId(final String postId) {
        return auditionIntegrationClient.getCommentsByPostId(postId);
    }

}
