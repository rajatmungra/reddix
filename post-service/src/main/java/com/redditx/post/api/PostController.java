package com.redditx.post.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.common.dto.PageResponse;
import com.redditx.post.application.PostService;
import com.redditx.post.dto.CreatePostRequest;
import com.redditx.post.dto.PostResponse;
import com.redditx.post.dto.UpdatePostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Create a new post")
    @PostMapping
    public ApiResponse<PostResponse> createPost(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePostRequest request
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        PostResponse response = postService.createPost(currentUserId, request);

        return ApiResponse.success("Post created successfully", response);
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(
            @PathVariable("postId") UUID postId
    ) {
        PostResponse response = postService.getPost(postId);

        return ApiResponse.success("Post fetched successfully", response);
    }

    @GetMapping("/community/{communityName}")
    public ApiResponse<PageResponse<PostResponse>> getPostsByCommunity(
            @PathVariable("communityName") String communityName,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size
    ) {
        PageResponse<PostResponse> response =
                postService.getPostsByCommunity(communityName, page, size);

        return ApiResponse.success("Community posts fetched successfully", response);
    }

    @GetMapping("/me")
    public ApiResponse<PageResponse<PostResponse>> getMyPosts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        PageResponse<PostResponse> response =
                postService.getMyPosts(currentUserId, page, size);

        return ApiResponse.success("My posts fetched successfully", response);
    }

    @PatchMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("postId") UUID postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        PostResponse response = postService.updatePost(currentUserId, postId, request);

        return ApiResponse.success("Post updated successfully", response);
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("postId") UUID postId
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        postService.deletePost(currentUserId, postId);

        return ApiResponse.success("Post deleted successfully", null);
    }
}