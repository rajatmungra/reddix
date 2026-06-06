package com.redditx.post.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.post.application.PostService;
import com.redditx.post.dto.PostResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/internal/posts")
public class InternalPostController {

    private final PostService postService;
    private final String internalSecret;

    public InternalPostController(
            PostService postService,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.postService = postService;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPostForInternal(
            @RequestHeader("X-Internal-Secret") String requestSecret,
            @PathVariable("postId") UUID postId
    ) {
        validateInternalSecret(requestSecret);

        PostResponse response = postService.getPostForInternal(postId);

        return ApiResponse.success("Post fetched successfully", response);
    }

    @PostMapping("/{postId}/comments/increment")
    public ApiResponse<Void> incrementCommentCount(
            @RequestHeader("X-Internal-Secret") String requestSecret,
            @PathVariable("postId") UUID postId
    ) {
        validateInternalSecret(requestSecret);

        postService.incrementCommentCount(postId);

        return ApiResponse.success("Comment count incremented successfully", null);
    }

    @PostMapping("/{postId}/comments/decrement")
    public ApiResponse<Void> decrementCommentCount(
            @RequestHeader("X-Internal-Secret") String requestSecret,
            @PathVariable("postId") UUID postId
    ) {
        validateInternalSecret(requestSecret);

        postService.decrementCommentCount(postId);

        return ApiResponse.success("Comment count decremented successfully", null);
    }

    private void validateInternalSecret(String requestSecret) {
        if (!internalSecret.equals(requestSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret");
        }
    }
}