package com.redditx.community.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "communities",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_communities_name", columnNames = "name")
        }
)
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    private String description;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "member_count", nullable = false)
    private long memberCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Community() {
    }

    public Community(String name, String displayName, String description, UUID ownerUserId) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.ownerUserId = ownerUserId;
        this.memberCount = 1;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void incrementMemberCount() {
        this.memberCount++;
    }

    public void decrementMemberCount() {
        if (this.memberCount > 0) {
            this.memberCount--;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}