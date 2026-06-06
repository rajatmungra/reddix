package com.redditx.comment.api;

import com.redditx.comment.application.CommentService;
import com.redditx.comment.dto.CommentResponse;
import com.redditx.comment.dto.VoteCountUpdateRequest;
import com.redditx.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/internal/comments")
public class InternalCommentController {

    private final CommentService commentService;
    private final String internalSecret;

    public InternalCommentController(
            CommentService commentService,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.commentService = commentService;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/{commentId}")
    public ApiResponse<CommentResponse> getCommentForInternal(
            @RequestHeader("X-Internal-Secret") String requestSecret,
            @PathVariable("commentId") UUID commentId
    ) {
        validateInternalSecret(requestSecret);

        CommentResponse response = commentService.getCommentForInternal(commentId);

        return ApiResponse.success("Comment fetched successfully", response);
    }

    @PostMapping("/{commentId}/votes/apply-delta")
    public ApiResponse<Void> applyVoteCountDelta(
            @RequestHeader("X-Internal-Secret") String requestSecret,
            @PathVariable("commentId") UUID commentId,
            @RequestBody VoteCountUpdateRequest request
    ) {
        validateInternalSecret(requestSecret);

        commentService.applyVoteCountDelta(commentId, request);

        return ApiResponse.success("Comment vote count updated successfully", null);
    }

    private void validateInternalSecret(String requestSecret) {
        if (!internalSecret.equals(requestSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret");
        }
    }
}