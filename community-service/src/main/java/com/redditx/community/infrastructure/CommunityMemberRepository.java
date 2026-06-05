package com.redditx.community.infrastructure;

import com.redditx.community.domain.CommunityMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {

    Optional<CommunityMember> findByCommunityIdAndUserId(UUID communityId, UUID userId);
}