package com.redditx.feed.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.common.dto.PageResponse;
import com.redditx.feed.application.FeedService;
import com.redditx.feed.dto.FeedItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @Operation(summary = "Get home feed")
    @GetMapping
    public ApiResponse<PageResponse<FeedItemResponse>> getHomeFeed(
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "20") int size
    ) {
        PageResponse<FeedItemResponse> response =
                feedService.getHomeFeed(page, size);

        return ApiResponse.success("Home feed fetched successfully", response);
    }

    @GetMapping("/community/{communityName}")
    public ApiResponse<PageResponse<FeedItemResponse>> getCommunityFeed(
            @PathVariable("communityName") String communityName,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "20") int size
    ) {
        PageResponse<FeedItemResponse> response =
                feedService.getCommunityFeed(communityName, page, size);

        return ApiResponse.success("Community feed fetched successfully", response);
    }
}