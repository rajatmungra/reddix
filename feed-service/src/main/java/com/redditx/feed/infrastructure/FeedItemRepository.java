package com.redditx.feed.infrastructure;

import com.redditx.feed.domain.FeedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeedItemRepository extends JpaRepository<FeedItem, UUID> {

    boolean existsByPostId(UUID postId);

    Page<FeedItem> findAllByOrderByPostCreatedAtDesc(Pageable pageable);

    Page<FeedItem> findByCommunityNameOrderByPostCreatedAtDesc(
            String communityName,
            Pageable pageable
    );
}