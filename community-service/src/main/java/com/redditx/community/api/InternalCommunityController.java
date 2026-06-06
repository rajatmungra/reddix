package com.redditx.community.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.community.application.CommunityService;
import com.redditx.community.dto.CommunityResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/communities")
public class InternalCommunityController {

    private final CommunityService communityService;
    private final String internalSecret;

    public InternalCommunityController(
            CommunityService communityService,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.communityService = communityService;
        this.internalSecret = internalSecret;
    }

    @GetMapping("/{name}")
    public ApiResponse<CommunityResponse> getCommunityForInternal(
            @RequestHeader("X-Internal-Secret") String requestSecret,
            @PathVariable("name") String name
    ) {
        if (!internalSecret.equals(requestSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret");
        }

        CommunityResponse response = communityService.getCommunityForInternal(name);

        return ApiResponse.success("Community fetched successfully", response);
    }
}