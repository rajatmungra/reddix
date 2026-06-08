package com.redditx.feed.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "feed_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_feed_items_post_id", columnNames = "post_id")
        },
        indexes = {
                @Index(name = "idx_feed_items_community_created", columnList = "community_name,post_created_at"),
                @Index(name = "idx_feed_items_created", columnList = "post_created_at"),
                @Index(name = "idx_feed_items_score", columnList = "score")
        }
)
public class FeedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "community_name", nullable = false, length = 50)
    private String communityName;

    @Column(name = "author_user_id", nullable = false)
    private UUID authorUserId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "post_type", nullable = false, length = 30)
    private String postType;

    @Column(nullable = false)
    private long score;

    @Column(name = "comment_count", nullable = false)
    private long commentCount;

    @Column(name = "post_created_at", nullable = false)
    private Instant postCreatedAt;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    protected FeedItem() {
    }

    public FeedItem(
            UUID postId,
            String communityName,
            UUID authorUserId,
            String title,
            String postType,
            Instant postCreatedAt
    ) {
        this.postId = postId;
        this.communityName = communityName;
        this.authorUserId = authorUserId;
        this.title = title;
        this.postType = postType;
        this.score = 0;
        this.commentCount = 0;
        this.postCreatedAt = postCreatedAt;
        this.ingestedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
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

    public String getPostType() {
        return postType;
    }

    public long getScore() {
        return score;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public Instant getPostCreatedAt() {
        return postCreatedAt;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }
}