package com.redditx.community.application;

import com.redditx.common.dto.PageResponse;
import com.redditx.community.domain.Community;
import com.redditx.community.domain.CommunityMember;
import com.redditx.community.domain.CommunityMemberRole;
import com.redditx.community.dto.CommunityResponse;
import com.redditx.community.dto.CreateCommunityRequest;
import com.redditx.community.infrastructure.CommunityMemberRepository;
import com.redditx.community.infrastructure.CommunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final CommunityMemberRepository communityMemberRepository;

    public CommunityService(
            CommunityRepository communityRepository,
            CommunityMemberRepository communityMemberRepository
    ) {
        this.communityRepository = communityRepository;
        this.communityMemberRepository = communityMemberRepository;
    }

    public CommunityResponse createCommunity(
            UUID currentUserId,
            CreateCommunityRequest request
    ) {
        String name = normalizeName(request.name());

        if (communityRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Community already exists");
        }

        Community community = new Community(
                name,
                request.displayName().trim(),
                trimNullable(request.description()),
                currentUserId
        );

        Community savedCommunity = communityRepository.save(community);

        CommunityMember ownerMembership = new CommunityMember(
                savedCommunity,
                currentUserId,
                CommunityMemberRole.OWNER
        );

        communityMemberRepository.save(ownerMembership);

        return toResponse(savedCommunity, Optional.of(ownerMembership));
    }

    public CommunityResponse getCommunity(String communityName, UUID currentUserId) {
        Community community = findCommunityByName(communityName);

        Optional<CommunityMember> membership =
                communityMemberRepository.findByCommunityIdAndUserId(
                        community.getId(),
                        currentUserId
                );

        return toResponse(community, membership);
    }

    public PageResponse<CommunityResponse> listCommunities(
            UUID currentUserId,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<Community> communities = communityRepository.findAll(
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return new PageResponse<>(
                communities.getContent()
                        .stream()
                        .map(community -> {
                            Optional<CommunityMember> membership =
                                    communityMemberRepository.findByCommunityIdAndUserId(
                                            community.getId(),
                                            currentUserId
                                    );

                            return toResponse(community, membership);
                        })
                        .toList(),
                communities.getNumber(),
                communities.getSize(),
                communities.getTotalElements(),
                communities.getTotalPages(),
                communities.isLast()
        );
    }

    public CommunityResponse joinCommunity(String communityName, UUID currentUserId) {
        Community community = findCommunityByName(communityName);

        Optional<CommunityMember> existingMembership =
                communityMemberRepository.findByCommunityIdAndUserId(
                        community.getId(),
                        currentUserId
                );

        if (existingMembership.isPresent()) {
            CommunityMember member = existingMembership.get();

            if (member.isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already joined this community");
            }

            member.rejoin();
            community.incrementMemberCount();

            communityRepository.save(community);
            CommunityMember savedMember = communityMemberRepository.save(member);

            return toResponse(community, Optional.of(savedMember));
        }

        CommunityMember newMember = new CommunityMember(
                community,
                currentUserId,
                CommunityMemberRole.MEMBER
        );

        community.incrementMemberCount();

        communityRepository.save(community);
        CommunityMember savedMember = communityMemberRepository.save(newMember);

        return toResponse(community, Optional.of(savedMember));
    }

    public CommunityResponse leaveCommunity(String communityName, UUID currentUserId) {
        Community community = findCommunityByName(communityName);

        CommunityMember member = communityMemberRepository
                .findByCommunityIdAndUserId(community.getId(), currentUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "User is not a member of this community"
                ));

        if (!member.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an active member");
        }

        if (member.isOwner()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Community owner cannot leave before transferring ownership"
            );
        }

        member.leave();
        community.decrementMemberCount();

        communityRepository.save(community);
        CommunityMember savedMember = communityMemberRepository.save(member);

        return toResponse(community, Optional.of(savedMember));
    }

    private Community findCommunityByName(String communityName) {
        String name = normalizeName(communityName);

        return communityRepository.findByName(name)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Community not found"
                ));
    }

    private CommunityResponse toResponse(
            Community community,
            Optional<CommunityMember> membership
    ) {
        boolean joined = membership
                .map(CommunityMember::isActive)
                .orElse(false);

        String role = membership
                .filter(CommunityMember::isActive)
                .map(member -> member.getRole().name())
                .orElse(null);

        return new CommunityResponse(
                community.getId(),
                community.getName(),
                community.getDisplayName(),
                community.getDescription(),
                community.getOwnerUserId(),
                community.getMemberCount(),
                joined,
                role,
                community.getCreatedAt()
        );
    }

    public CommunityResponse getCommunityForInternal(String communityName) {
        Community community = findCommunityByName(communityName);
        return toResponse(community, Optional.empty());
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase();
    }

    private String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}