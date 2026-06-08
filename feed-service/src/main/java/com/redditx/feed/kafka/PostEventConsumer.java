package com.redditx.feed.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.event.EventTopics;
import com.redditx.feed.application.FeedService;
import com.redditx.feed.event.PostCreatedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PostEventConsumer {

    private final ObjectMapper objectMapper;
    private final FeedService feedService;

    public PostEventConsumer(
            ObjectMapper objectMapper,
            FeedService feedService
    ) {
        this.objectMapper = objectMapper;
        this.feedService = feedService;
    }

    @KafkaListener(
            topics = EventTopics.POST_EVENTS,
            groupId = "feed-service-group"
    )
    public void consumePostEvent(String message) {
        try {
            PostCreatedEvent event = objectMapper.readValue(
                    message,
                    PostCreatedEvent.class
            );

            feedService.handlePostCreated(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Could not parse PostCreatedEvent", ex);
        }
    }
}