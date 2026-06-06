package com.redditx.post.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "idx_posts_community_name", columnList = "community_name"),
                @Index(name = "idx_posts_author_user_id", columnList = "author_user_id"),
                @Index(name = "idx_posts_created_at", columnList = "created_at")
        }
)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "community_name", nullable = false, length = 50)
    private String communityName;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private PostType postType;

    @Column(name = "upvote_count", nullable = false)
    private long upvoteCount;

    @Column(name = "downvote_count", nullable = false)
    private long downvoteCount;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Post() {
    }

    public Post(
            String communityName,
            UUID authorUserId,
            String title,
            String content,
            String url,
            PostType postType
    ) {
        this.communityName = communityName;
        this.authorUserId = authorUserId;
        this.title = title;
        this.content = content;
        this.url = url;
        this.postType = postType;
        this.upvoteCount = 0;
        this.downvoteCount = 0;
        this.commentCount = 0;
        this.deleted = false;
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

    public void update(String title, String content, String url) {
        if (title != null && !title.isBlank()) {
            this.title = title.trim();
        }

        if (content != null) {
            this.content = content.trim();
        }

        if (url != null) {
            this.url = url.trim();
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public boolean isAuthor(UUID userId) {
        return this.authorUserId.equals(userId);
    }

    public long getScore() {
        return upvoteCount - downvoteCount;
    }

    public UUID getId() {
        return id;
    }

    public String getCommunityName() {
        return communityName;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public PostType getPostType() {
        return postType;
    }

    public long getUpvoteCount() {
        return upvoteCount;
    }

    public long getDownvoteCount() {
        return downvoteCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}