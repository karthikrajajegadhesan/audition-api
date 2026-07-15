package com.audition.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionComment;
import com.audition.model.AuditionPost;
import com.audition.service.AuditionService;
import com.audition.web.advice.ExceptionControllerAdvice;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuditionControllerTest {

    @Mock
    private AuditionService auditionService;

    @Mock
    private AuditionLogger auditionLogger;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuditionController(auditionService, auditionLogger))
            .setControllerAdvice(new ExceptionControllerAdvice(auditionLogger))
            .build();
    }

    @Test
    void getPosts_returnsAllPostsWhenNoFiltersApplied() throws Exception {
        when(auditionService.getPosts(null, null)).thenReturn(List.of(
            createPost(1, 1, "sunt aut facere", "body one"),
            createPost(2, 2, "qui est esse", "body two")));

        mockMvc.perform(get("/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("sunt aut facere"));

        verify(auditionService).getPosts(null, null);
    }

    @Test
    void getPosts_filtersByUserId() throws Exception {
        when(auditionService.getPosts(1, null)).thenReturn(List.of(
            createPost(1, 1, "title one", "body one")));

        mockMvc.perform(get("/posts").param("userId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].userId").value(1));

        verify(auditionService).getPosts(1, null);
    }

    @Test
    void getPosts_filtersByTitleCaseInsensitively() throws Exception {
        when(auditionService.getPosts(isNull(), eq("sunt"))).thenReturn(List.of(
            createPost(1, 1, "SUNT aut facere", "body one")));

        mockMvc.perform(get("/posts").param("title", "sunt"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1));

        verify(auditionService).getPosts(isNull(), eq("sunt"));
    }

    @Test
    void getPosts_filtersByUserIdAndTitle() throws Exception {
        when(auditionService.getPosts(1, "matching")).thenReturn(List.of(
            createPost(1, 1, "matching title", "body one")));

        mockMvc.perform(get("/posts").param("userId", "1").param("title", "matching"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1));

        verify(auditionService).getPosts(1, "matching");
    }

    @Test
    void getPostById_returnsPostForValidId() throws Exception {
        final AuditionPost post = createPost(1, 1, "title", "body");
        when(auditionService.getPostById("1")).thenReturn(post);

        mockMvc.perform(get("/posts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("title"));

        verify(auditionService).getPostById("1");
    }

    @Test
    void getPostById_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/posts/abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.detail").value("Invalid post id: abc"));
    }

    @Test
    void getPostById_zeroId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/posts/0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Invalid post id: 0"));
    }

    @Test
    void getPostWithComments_returnsPostWithComments() throws Exception {
        final AuditionPost post = createPost(1, 1, "title", "body");
        post.setComments(List.of(createComment(1, 1, "comment body")));
        when(auditionService.getPostWithComments("1")).thenReturn(post);

        mockMvc.perform(get("/posts/1/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.comments.length()").value(1))
            .andExpect(jsonPath("$.comments[0].body").value("comment body"));

        verify(auditionService).getPostWithComments("1");
    }

    @Test
    void getCommentsByPostId_returnsComments() throws Exception {
        when(auditionService.getCommentsByPostId("1"))
            .thenReturn(List.of(createComment(1, 1, "comment body")));

        mockMvc.perform(get("/comments").param("postId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].body").value("comment body"));

        verify(auditionService).getCommentsByPostId(eq("1"));
    }

    @Test
    void getCommentsByPostId_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/comments").param("postId", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Invalid post id: -1"));
    }

    @Test
    void getPostById_notFoundFromService_returnsNotFound() throws Exception {
        when(auditionService.getPostById("99"))
            .thenThrow(new SystemException("Cannot find a Post with id 99", "Resource Not Found", 404));

        mockMvc.perform(get("/posts/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource Not Found"));
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
