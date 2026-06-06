package com.redditx.vote.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.vote.application.VoteService;
import com.redditx.vote.domain.VoteTargetType;
import com.redditx.vote.dto.VoteRequest;
import com.redditx.vote.dto.VoteResponse;
import com.redditx.vote.dto.VoteStatusResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping
    public ApiResponse<VoteResponse> vote(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody VoteRequest request
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        VoteResponse response = voteService.vote(currentUserId, request);

        return ApiResponse.success("Vote saved successfully", response);
    }

    @DeleteMapping("/{targetType}/{targetId}")
    public ApiResponse<Void> removeVote(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("targetType") VoteTargetType targetType,
            @PathVariable("targetId") UUID targetId
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        voteService.removeVote(currentUserId, targetType, targetId);

        return ApiResponse.success("Vote removed successfully", null);
    }

    @GetMapping("/{targetType}/{targetId}/me")
    public ApiResponse<VoteStatusResponse> getMyVoteStatus(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("targetType") VoteTargetType targetType,
            @PathVariable("targetId") UUID targetId
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        VoteStatusResponse response =
                voteService.getMyVoteStatus(currentUserId, targetType, targetId);

        return ApiResponse.success("Vote status fetched successfully", response);
    }
}