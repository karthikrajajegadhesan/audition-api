package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import java.util.List;
import java.util.Locale;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class AuditionService {

    private final AuditionIntegrationClient auditionIntegrationClient;

    public AuditionService(final AuditionIntegrationClient auditionIntegrationClient) {
        this.auditionIntegrationClient = auditionIntegrationClient;
    }

    public List<AuditionPost> getPosts(final Integer userId, final String title) {
        return auditionIntegrationClient.getPosts().stream()
            .filter(post -> userId == null || post.getUserId() == userId)
            .filter(post -> title == null || post.getTitle() != null
                && post.getTitle().toLowerCase(Locale.ROOT).contains(title.toLowerCase(Locale.ROOT)))
            .toList();
    }

    public AuditionPost getPostById(final String postId) {
        return auditionIntegrationClient.getPostById(postId);
    }

    public AuditionPost getPostWithComments(final String postId) {
        return auditionIntegrationClient.getPostWithComments(postId);
    }

    public List<AuditionComment> getCommentsByPostId(final String postId) {
        return auditionIntegrationClient.getCommentsByPostId(postId);
    }

}
