package com.redditx.feed.application;

import com.redditx.common.dto.PageResponse;
import com.redditx.feed.domain.FeedItem;
import com.redditx.feed.domain.ProcessedEvent;
import com.redditx.feed.dto.FeedItemResponse;
import com.redditx.feed.event.PostCreatedEvent;
import com.redditx.feed.infrastructure.FeedItemRepository;
import com.redditx.feed.infrastructure.ProcessedEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedService {

    private final FeedItemRepository feedItemRepository;
    private final ProcessedEventRepository processedEventRepository;

    public FeedService(
            FeedItemRepository feedItemRepository,
            ProcessedEventRepository processedEventRepository
    ) {
        this.feedItemRepository = feedItemRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void handlePostCreated(PostCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        if (!feedItemRepository.existsByPostId(event.postId())) {
            FeedItem feedItem = new FeedItem(
                    event.postId(),
                    event.communityName(),
                    event.authorUserId(),
                    event.title(),
                    event.postType(),
                    event.createdAt()
            );

            feedItemRepository.save(feedItem);
        }

        processedEventRepository.save(
                new ProcessedEvent(event.eventId(), "PostCreatedEvent")
        );
    }

    public PageResponse<FeedItemResponse> getHomeFeed(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<FeedItem> feedItems = feedItemRepository.findAllByOrderByPostCreatedAtDesc(
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "postCreatedAt")
                )
        );

        return toPageResponse(feedItems);
    }

    public PageResponse<FeedItemResponse> getCommunityFeed(
            String communityName,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<FeedItem> feedItems = feedItemRepository.findByCommunityNameOrderByPostCreatedAtDesc(
                communityName.trim().toLowerCase(),
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "postCreatedAt")
                )
        );

        return toPageResponse(feedItems);
    }

    private PageResponse<FeedItemResponse> toPageResponse(Page<FeedItem> feedItems) {
        return new PageResponse<>(
                feedItems.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                feedItems.getNumber(),
                feedItems.getSize(),
                feedItems.getTotalElements(),
                feedItems.getTotalPages(),
                feedItems.isLast()
        );
    }

    private FeedItemResponse toResponse(FeedItem feedItem) {
        return new FeedItemResponse(
                feedItem.getId(),
                feedItem.getPostId(),
                feedItem.getCommunityName(),
                feedItem.getAuthorUserId(),
                feedItem.getTitle(),
                feedItem.getPostType(),
                feedItem.getScore(),
                feedItem.getCommentCount(),
                feedItem.getPostCreatedAt()
        );
    }
}