package com.redditx.vote.infrastructure;

import com.redditx.vote.domain.Vote;
import com.redditx.vote.domain.VoteTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {

    Optional<Vote> findByUserIdAndTargetTypeAndTargetId(
            UUID userId,
            VoteTargetType targetType,
            UUID targetId
    );
}