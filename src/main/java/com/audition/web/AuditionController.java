package com.audition.web;

import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import com.audition.web.validation.ValidPostId;
import java.util.List;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for posts and comments. Delegates to AuditionService and validates
 * incoming post identifiers before they reach the service layer.
 */
@RestController
@Validated
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

    /**
     * Returns posts, optionally filtered by userId and title.
     */
    @GetMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionPost> getPosts(
        @RequestParam(value = "userId", required = false) final Integer userId,
        @RequestParam(value = "title", required = false) final String title) {
        logInfo(String.format("Received request to fetch posts with userId=%s, title=%s", userId, title));
        return auditionService.getPosts(userId, title);
    }

    /**
     * Returns a single post by post id.
     */
    @GetMapping(value = "/posts/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostById(@PathVariable("id") @ValidPostId final String postId) {
        logInfo(String.format("Received request to fetch post with id=%s", postId));
        return auditionService.getPostById(postId);
    }

    /**
     * Returns a post with its comments embedded in the response.
     */
    @GetMapping(value = "/posts/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public AuditionPost getPostWithComments(@PathVariable("id") @ValidPostId final String postId) {
        logInfo(String.format("Received request to fetch post with comments for id=%s", postId));
        return auditionService.getPostWithComments(postId);
    }

    /**
     * Returns comments for a post as a list.
     */
    @GetMapping(value = "/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AuditionComment> getCommentsByPostId(
        @RequestParam("postId") @ValidPostId final String postId) {
        logInfo(String.format("Received request to fetch comments for post id=%s", postId));
        return auditionService.getCommentsByPostId(postId);
    }

    private void logInfo(final String message) {
        if (LOG.isInfoEnabled()) {
            auditionLogger.info(LOG, message);
        }
    }

}
