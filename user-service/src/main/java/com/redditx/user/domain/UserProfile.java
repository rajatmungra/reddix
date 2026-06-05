package com.redditx.user.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_profiles_auth_user_id", columnNames = "authUserId"),
                @UniqueConstraint(name = "uk_user_profiles_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_user_profiles_email", columnNames = "email")
        }
)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID authUserId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 100)
    private String displayName;

    @Column(length = 500)
    private String bio;

    private String avatarUrl;

    @Column(nullable = false)
    private long postKarma;

    @Column(nullable = false)
    private long commentKarma;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
    }

    public UserProfile(UUID authUserId, String username, String email) {
        this.authUserId = authUserId;
        this.username = username;
        this.email = email;
        this.displayName = username;
        this.postKarma = 0;
        this.commentKarma = 0;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateProfile(String displayName, String bio, String avatarUrl) {
        if (displayName != null && !displayName.isBlank()) {
            this.displayName = displayName.trim();
        }

        if (bio != null) {
            this.bio = bio.trim();
        }

        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl.trim();
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthUserId() {
        return authUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public long getPostKarma() {
        return postKarma;
    }

    public long getCommentKarma() {
        return commentKarma;
    }

    public long getTotalKarma() {
        return postKarma + commentKarma;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}