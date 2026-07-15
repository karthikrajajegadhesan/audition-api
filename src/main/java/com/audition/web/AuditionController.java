package com.audition.web;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import java.util.List;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Getter
public class AuditionController {

    private static final Logger LOG = LoggerFactory.getLogger(AuditionController.class);

    private final AuditionService auditionService;
    private final AuditionLogger auditionLogger;

    public AuditionController(final AuditionService auditionService,
        final AuditionLogger auditionLogger) {
        this.auditionService = auditionService;
        this.auditionLogger = auditionLogger;
    }

    @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPost> getPosts(
        @RequestParam(value = "userId", required = false) final Integer userId,
        @RequestParam(value = "title", required = false) final String title) {
        logInfo(String.format("Received request to fetch posts with userId=%s, title=%s", userId, title));
        final List<AuditionPost> posts = auditionService.getPosts(userId, title);
        logInfo(String.format("Returning %d posts", posts.size()));
        return posts;
    }

    @GetMapping(value = "/posts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostById(@PathVariable("id") final String postId) {
        logInfo(String.format("Received request to fetch post with id=%s", postId));
        validatePostId(postId);
        final AuditionPost post = auditionService.getPostById(postId);
        logInfo(String.format("Returning post with id=%s", postId));
        return post;
    }

    @GetMapping(value = "/posts/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostWithComments(@PathVariable("id") final String postId) {
        logInfo(String.format("Received request to fetch post with comments for id=%s", postId));
        validatePostId(postId);
        final AuditionPost post = auditionService.getPostWithComments(postId);
        logInfo(String.format("Returning post with comments for id=%s", postId));
        return post;
    }

    @GetMapping(value = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionComment> getCommentsByPostId(
        @RequestParam("postId") final String postId) {
        logInfo(String.format("Received request to fetch comments for post id=%s", postId));
        validatePostId(postId);
        final List<AuditionComment> comments = auditionService.getCommentsByPostId(postId);
        logInfo(String.format("Returning %d comments for post id=%s", comments.size(), postId));
        return comments;
    }

    private void validatePostId(final String postId) {
        if (postId == null || !postId.matches("\\d+") || Integer.parseInt(postId) <= 0) {
            if (LOG.isWarnEnabled()) {
                auditionLogger.warn(LOG, String.format("Invalid post id received: %s", postId));
            }
            throw new SystemException("Invalid post id: " + postId, "Bad Request", 400);
        }
    }

    private void logInfo(final String message) {
        if (LOG.isInfoEnabled()) {
            auditionLogger.info(LOG, message);
        }
    }

}
