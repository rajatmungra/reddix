package com.redditx.vote.application;

import com.redditx.vote.client.CommentTargetClient;
import com.redditx.vote.client.PostTargetClient;
import com.redditx.vote.domain.Vote;
import com.redditx.vote.domain.VoteTargetType;
import com.redditx.vote.domain.VoteType;
import com.redditx.vote.dto.VoteRequest;
import com.redditx.vote.dto.VoteResponse;
import com.redditx.vote.dto.VoteStatusResponse;
import com.redditx.vote.infrastructure.VoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostTargetClient postTargetClient;
    private final CommentTargetClient commentTargetClient;

    public VoteService(
            VoteRepository voteRepository,
            PostTargetClient postTargetClient,
            CommentTargetClient commentTargetClient
    ) {
        this.voteRepository = voteRepository;
        this.postTargetClient = postTargetClient;
        this.commentTargetClient = commentTargetClient;
    }

    @Transactional
    public VoteResponse vote(UUID currentUserId, VoteRequest request) {
        validateTargetExists(request.targetType(), request.targetId());

        Optional<Vote> existingVoteOptional =
                voteRepository.findByUserIdAndTargetTypeAndTargetId(
                        currentUserId,
                        request.targetType(),
                        request.targetId()
                );

        if (existingVoteOptional.isEmpty()) {
            Vote vote = new Vote(
                    currentUserId,
                    request.targetType(),
                    request.targetId(),
                    request.voteType()
            );

            Vote savedVote = voteRepository.save(vote);

            int upvoteDelta = request.voteType() == VoteType.UPVOTE ? 1 : 0;
            int downvoteDelta = request.voteType() == VoteType.DOWNVOTE ? 1 : 0;

            applyVoteDelta(request.targetType(), request.targetId(), upvoteDelta, downvoteDelta);

            return toResponse(savedVote);
        }

        Vote existingVote = existingVoteOptional.get();

        if (existingVote.getVoteType() == request.voteType()) {
            return toResponse(existingVote);
        }

        VoteType oldVoteType = existingVote.getVoteType();

        existingVote.changeVoteType(request.voteType());

        Vote savedVote = voteRepository.save(existingVote);

        int upvoteDelta = 0;
        int downvoteDelta = 0;

        if (oldVoteType == VoteType.UPVOTE && request.voteType() == VoteType.DOWNVOTE) {
            upvoteDelta = -1;
            downvoteDelta = 1;
        } else if (oldVoteType == VoteType.DOWNVOTE && request.voteType() == VoteType.UPVOTE) {
            downvoteDelta = -1;
            upvoteDelta = 1;
        }

        applyVoteDelta(request.targetType(), request.targetId(), upvoteDelta, downvoteDelta);

        return toResponse(savedVote);
    }

    @Transactional
    public void removeVote(
            UUID currentUserId,
            VoteTargetType targetType,
            UUID targetId
    ) {
        Vote vote = voteRepository.findByUserIdAndTargetTypeAndTargetId(
                        currentUserId,
                        targetType,
                        targetId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vote not found"
                ));

        voteRepository.delete(vote);

        int upvoteDelta = vote.getVoteType() == VoteType.UPVOTE ? -1 : 0;
        int downvoteDelta = vote.getVoteType() == VoteType.DOWNVOTE ? -1 : 0;

        applyVoteDelta(targetType, targetId, upvoteDelta, downvoteDelta);
    }

    public VoteStatusResponse getMyVoteStatus(
            UUID currentUserId,
            VoteTargetType targetType,
            UUID targetId
    ) {
        Optional<Vote> voteOptional =
                voteRepository.findByUserIdAndTargetTypeAndTargetId(
                        currentUserId,
                        targetType,
                        targetId
                );

        return voteOptional
                .map(vote -> new VoteStatusResponse(
                        targetType,
                        targetId,
                        true,
                        vote.getVoteType()
                ))
                .orElseGet(() -> new VoteStatusResponse(
                        targetType,
                        targetId,
                        false,
                        null
                ));
    }

    private void validateTargetExists(VoteTargetType targetType, UUID targetId) {
        try {
            if (targetType == VoteTargetType.POST) {
                postTargetClient.validatePostExists(targetId);
            } else if (targetType == VoteTargetType.COMMENT) {
                commentTargetClient.validateCommentExists(targetId);
            }
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Target does not exist or target service is unavailable"
            );
        }
    }

    private void applyVoteDelta(
            VoteTargetType targetType,
            UUID targetId,
            int upvoteDelta,
            int downvoteDelta
    ) {
        try {
            if (targetType == VoteTargetType.POST) {
                postTargetClient.applyVoteDelta(targetId, upvoteDelta, downvoteDelta);
            } else if (targetType == VoteTargetType.COMMENT) {
                commentTargetClient.applyVoteDelta(targetId, upvoteDelta, downvoteDelta);
            }
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not update target vote count"
            );
        }
    }

    private VoteResponse toResponse(Vote vote) {
        return new VoteResponse(
                vote.getId(),
                vote.getUserId(),
                vote.getTargetType(),
                vote.getTargetId(),
                vote.getVoteType(),
                vote.getCreatedAt(),
                vote.getUpdatedAt()
        );
    }
}