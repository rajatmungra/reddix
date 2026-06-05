package com.redditx.community.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.common.dto.PageResponse;
import com.redditx.community.application.CommunityService;
import com.redditx.community.dto.CommunityResponse;
import com.redditx.community.dto.CreateCommunityRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping
    public ApiResponse<CommunityResponse> createCommunity(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCommunityRequest request
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        CommunityResponse response =
                communityService.createCommunity(currentUserId, request);

        return ApiResponse.success("Community created successfully", response);
    }

    @GetMapping("/{name}")
    public ApiResponse<CommunityResponse> getCommunity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("name") String name
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        CommunityResponse response =
                communityService.getCommunity(name, currentUserId);

        return ApiResponse.success("Community fetched successfully", response);
    }

    @GetMapping
    public ApiResponse<PageResponse<CommunityResponse>> listCommunities(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        PageResponse<CommunityResponse> response =
                communityService.listCommunities(currentUserId, page, size);

        return ApiResponse.success("Communities fetched successfully", response);
    }

    @PostMapping("/{name}/join")
    public ApiResponse<CommunityResponse> joinCommunity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("name") String name
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        CommunityResponse response =
                communityService.joinCommunity(name, currentUserId);

        return ApiResponse.success("Community joined successfully", response);
    }

    @DeleteMapping("/{name}/leave")
    public ApiResponse<CommunityResponse> leaveCommunity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("name") String name
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        CommunityResponse response =
                communityService.leaveCommunity(name, currentUserId);

        return ApiResponse.success("Community left successfully", response);
    }
}