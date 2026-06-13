package com.redditx.feed.application;

import com.redditx.common.dto.PageResponse;
import com.redditx.feed.cache.FeedCacheService;
import com.redditx.feed.domain.FeedItem;
import com.redditx.feed.domain.ProcessedEvent;
import com.redditx.feed.dto.FeedItemResponse;
import com.redditx.feed.event.CommentCreatedEvent;
import com.redditx.feed.event.CommentDeletedEvent;
import com.redditx.feed.event.PostCreatedEvent;
import com.redditx.feed.event.VoteChangedEvent;
import com.redditx.feed.infrastructure.FeedItemRepository;
import com.redditx.feed.infrastructure.ProcessedEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class FeedService {

    private final FeedItemRepository feedItemRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final FeedCacheService feedCacheService;

    public FeedService(
            FeedItemRepository feedItemRepository,
            ProcessedEventRepository processedEventRepository,
            FeedCacheService feedCacheService
    ) {
        this.feedItemRepository = feedItemRepository;
        this.processedEventRepository = processedEventRepository;
        this.feedCacheService = feedCacheService;
    }

    @Transactional
    public void handlePostCreated(PostCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        boolean created = false;

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
            created = true;
        }

        processedEventRepository.save(
                new ProcessedEvent(event.eventId(), "PostCreatedEvent")
        );

        if (created) {
            feedCacheService.bumpHomeFeedVersion();
            feedCacheService.bumpCommunityFeedVersion(event.communityName());
        }
    }

    public PageResponse<FeedItemResponse> getHomeFeed(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Optional<PageResponse<FeedItemResponse>> cachedFeed =
                feedCacheService.getHomeFeed(safePage, safeSize);

        if (cachedFeed.isPresent()) {
            return cachedFeed.get();
        }

        Page<FeedItem> feedItems = feedItemRepository.findAllByOrderByPostCreatedAtDesc(
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "postCreatedAt")
                )
        );

        PageResponse<FeedItemResponse> response = toPageResponse(feedItems);

        feedCacheService.putHomeFeed(safePage, safeSize, response);

        return response;
    }

    public PageResponse<FeedItemResponse> getCommunityFeed(
            String communityName,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String normalizedCommunityName = communityName.trim().toLowerCase();

        Optional<PageResponse<FeedItemResponse>> cachedFeed =
                feedCacheService.getCommunityFeed(
                        normalizedCommunityName,
                        safePage,
                        safeSize
                );

        if (cachedFeed.isPresent()) {
            return cachedFeed.get();
        }

        Page<FeedItem> feedItems = feedItemRepository.findByCommunityNameOrderByPostCreatedAtDesc(
                normalizedCommunityName,
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "postCreatedAt")
                )
        );

        PageResponse<FeedItemResponse> response = toPageResponse(feedItems);

        feedCacheService.putCommunityFeed(
                normalizedCommunityName,
                safePage,
                safeSize,
                response
        );

        return response;
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

    @Transactional
    public void handleCommentCreated(CommentCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        feedItemRepository.findByPostId(event.postId())
                .ifPresent(feedItem -> {
                    feedItem.incrementCommentCount();
                    feedItemRepository.save(feedItem);

                    feedCacheService.bumpHomeFeedVersion();
                    feedCacheService.bumpCommunityFeedVersion(feedItem.getCommunityName());
                });

        processedEventRepository.save(
                new ProcessedEvent(event.eventId(), "CommentCreatedEvent")
        );
    }

    @Transactional
    public void handleCommentDeleted(CommentDeletedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        feedItemRepository.findByPostId(event.postId())
                .ifPresent(feedItem -> {
                    feedItem.decrementCommentCount();
                    feedItemRepository.save(feedItem);

                    feedCacheService.bumpHomeFeedVersion();
                    feedCacheService.bumpCommunityFeedVersion(feedItem.getCommunityName());
                });

        processedEventRepository.save(
                new ProcessedEvent(event.eventId(), "CommentDeletedEvent")
        );
    }

    @Transactional
    public void handleVoteChanged(VoteChangedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        if ("POST".equalsIgnoreCase(event.targetType())) {
            feedItemRepository.findByPostId(event.targetId())
                    .ifPresent(feedItem -> {
                        feedItem.applyVoteDelta(
                                event.upvoteDelta(),
                                event.downvoteDelta()
                        );

                        feedItemRepository.save(feedItem);

                        feedCacheService.bumpHomeFeedVersion();
                        feedCacheService.bumpCommunityFeedVersion(feedItem.getCommunityName());
                    });
        }

        processedEventRepository.save(
                new ProcessedEvent(event.eventId(), "VoteChangedEvent")
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