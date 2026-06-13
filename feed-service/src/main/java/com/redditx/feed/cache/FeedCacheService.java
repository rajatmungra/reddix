package com.redditx.feed.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.dto.PageResponse;
import com.redditx.feed.dto.FeedItemResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class FeedCacheService {

    private static final Duration FEED_CACHE_TTL = Duration.ofSeconds(60);

    private static final String HOME_FEED_VERSION_KEY = "feed:home:version";
    private static final String COMMUNITY_FEED_VERSION_PREFIX = "feed:community:version:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public FeedCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<PageResponse<FeedItemResponse>> getHomeFeed(int page, int size) {
        return getFeed(buildHomeFeedKey(page, size));
    }

    public void putHomeFeed(
            int page,
            int size,
            PageResponse<FeedItemResponse> response
    ) {
        putFeed(buildHomeFeedKey(page, size), response);
    }

    public Optional<PageResponse<FeedItemResponse>> getCommunityFeed(
            String communityName,
            int page,
            int size
    ) {
        return getFeed(buildCommunityFeedKey(communityName, page, size));
    }

    public void putCommunityFeed(
            String communityName,
            int page,
            int size,
            PageResponse<FeedItemResponse> response
    ) {
        putFeed(buildCommunityFeedKey(communityName, page, size), response);
    }

    public void bumpHomeFeedVersion() {
        incrementVersion(HOME_FEED_VERSION_KEY);
    }

    public void bumpCommunityFeedVersion(String communityName) {
        incrementVersion(COMMUNITY_FEED_VERSION_PREFIX + normalizeCommunityName(communityName));
    }

    private Optional<PageResponse<FeedItemResponse>> getFeed(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null || json.isBlank()) {
                return Optional.empty();
            }

            PageResponse<FeedItemResponse> response = objectMapper.readValue(
                    json,
                    new TypeReference<PageResponse<FeedItemResponse>>() {
                    }
            );

            return Optional.of(response);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private void putFeed(
            String key,
            PageResponse<FeedItemResponse> response
    ) {
        try {
            String json = objectMapper.writeValueAsString(response);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    FEED_CACHE_TTL
            );
        } catch (Exception ex) {
            // Cache failure should not break feed API.
        }
    }

    private void incrementVersion(String key) {
        try {
            redisTemplate.opsForValue().increment(key);
        } catch (Exception ex) {
            // Cache invalidation failure should not break event processing.
        }
    }

    private String buildHomeFeedKey(int page, int size) {
        String version = getVersion(HOME_FEED_VERSION_KEY);

        return "feed:home:v:" + version + ":page:" + page + ":size:" + size;
    }

    private String buildCommunityFeedKey(String communityName, int page, int size) {
        String normalizedCommunityName = normalizeCommunityName(communityName);
        String versionKey = COMMUNITY_FEED_VERSION_PREFIX + normalizedCommunityName;
        String version = getVersion(versionKey);

        return "feed:community:"
                + normalizedCommunityName
                + ":v:"
                + version
                + ":page:"
                + page
                + ":size:"
                + size;
    }

    private String getVersion(String key) {
        try {
            String version = redisTemplate.opsForValue().get(key);
            return version == null ? "0" : version;
        } catch (Exception ex) {
            return "0";
        }
    }

    private String normalizeCommunityName(String communityName) {
        return communityName.trim().toLowerCase();
    }
}