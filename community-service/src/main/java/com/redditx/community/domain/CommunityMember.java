package com.redditx.community.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "community_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_community_members_community_user",
                        columnNames = {"community_id", "user_id"}
                )
        }
)
public class CommunityMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommunityMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CommunityMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CommunityMember() {
    }

    public CommunityMember(Community community, UUID userId, CommunityMemberRole role) {
        this.community = community;
        this.userId = userId;
        this.role = role;
        this.status = CommunityMemberStatus.ACTIVE;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.joinedAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void leave() {
        this.status = CommunityMemberStatus.LEFT;
    }

    public void rejoin() {
        this.status = CommunityMemberStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == CommunityMemberStatus.ACTIVE;
    }

    public boolean isOwner() {
        return this.role == CommunityMemberRole.OWNER;
    }

    public CommunityMemberRole getRole() {
        return role;
    }

    public CommunityMemberStatus getStatus() {
        return status;
    }
}