package com.redditx.comment.api;

import com.redditx.comment.application.CommentService;
import com.redditx.comment.dto.CommentResponse;
import com.redditx.comment.dto.CreateCommentRequest;
import com.redditx.comment.dto.UpdateCommentRequest;
import com.redditx.common.dto.ApiResponse;
import com.redditx.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<CommentResponse> createComment(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        CommentResponse response = commentService.createComment(currentUserId, request);

        return ApiResponse.success("Comment created successfully", response);
    }

    @GetMapping("/post/{postId}")
    public ApiResponse<PageResponse<CommentResponse>> getCommentsByPost(
            @PathVariable("postId") UUID postId,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "50") int size
    ) {
        PageResponse<CommentResponse> response =
                commentService.getCommentsByPost(postId, page, size);

        return ApiResponse.success("Post comments fetched successfully", response);
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<CommentResponse>> getMyComments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "20") int size
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        PageResponse<CommentResponse> response =
                commentService.getMyComments(currentUserId, page, size);

        return ApiResponse.success("My comments fetched successfully", response);
    }

    @PatchMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("commentId") UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        CommentResponse response =
                commentService.updateComment(currentUserId, commentId, request);

        return ApiResponse.success("Comment updated successfully", response);
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("commentId") UUID commentId
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        commentService.deleteComment(currentUserId, commentId);

        return ApiResponse.success("Comment deleted successfully", null);
    }
}