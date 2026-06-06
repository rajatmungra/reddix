package com.redditx.comment.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "comments",
        indexes = {
                @Index(name = "idx_comments_post_id", columnList = "post_id"),
                @Index(name = "idx_comments_author_user_id", columnList = "author_user_id"),
                @Index(name = "idx_comments_parent_comment_id", columnList = "parent_comment_id"),
                @Index(name = "idx_comments_created_at", columnList = "created_at")
        }
)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
    private boolean deleted;

    @Column(name = "upvote_count", nullable = false)
    private long upvoteCount;

    @Column(name = "downvote_count", nullable = false)
    private long downvoteCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Comment() {
    }

    public Comment(
            UUID postId,
            UUID authorUserId,
            UUID parentCommentId,
            String content,
            int depth
    ) {
        this.postId = postId;
        this.authorUserId = authorUserId;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.depth = depth;
        this.deleted = false;
        this.upvoteCount = 0;
        this.downvoteCount = 0;
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

    public void updateContent(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content.trim();
        }
    }

    public void markDeleted() {
        this.deleted = true;
        this.content = "[deleted]";
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

    public UUID getPostId() {
        return postId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public String getContent() {
        return content;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getUpvoteCount() {
        return upvoteCount;
    }

    public long getDownvoteCount() {
        return downvoteCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}