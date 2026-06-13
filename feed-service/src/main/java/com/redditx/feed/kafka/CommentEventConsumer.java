package com.redditx.feed.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.event.EventTopics;
import com.redditx.common.event.EventTypes;
import com.redditx.feed.application.DeadLetterService;
import com.redditx.feed.application.FeedService;
import com.redditx.feed.event.CommentCreatedEvent;
import com.redditx.feed.event.CommentDeletedEvent;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CommentEventConsumer {

    private final ObjectMapper objectMapper;
    private final FeedService feedService;
    private final DeadLetterService deadLetterService;

    public CommentEventConsumer(
            ObjectMapper objectMapper,
            FeedService feedService,
            DeadLetterService deadLetterService
    ) {
        this.objectMapper = objectMapper;
        this.feedService = feedService;
        this.deadLetterService = deadLetterService;
    }

    @RetryableTopic(
            attempts = "4",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "true",
            numPartitions = "3",
            replicationFactor = "1",
            retryTopicSuffix = ".retry",
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(
            topics = EventTopics.COMMENT_EVENTS,
            groupId = "feed-service-group-v4"
    )
    public void consumeCommentEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);

            String eventType = root.get("eventType").asText();
            JsonNode payloadNode = root.get("payload");

            if (EventTypes.COMMENT_CREATED.equals(eventType)) {
                CommentCreatedEvent event = objectMapper.treeToValue(
                        payloadNode,
                        CommentCreatedEvent.class
                );

                feedService.handleCommentCreated(event);
                return;
            }

            if (EventTypes.COMMENT_DELETED.equals(eventType)) {
                CommentDeletedEvent event = objectMapper.treeToValue(
                        payloadNode,
                        CommentDeletedEvent.class
                );

                feedService.handleCommentDeleted(event);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not process comment event", ex);
        }
    }

    @DltHandler
    public void handleDlt(
            String message,
            @Headers Map<String, Object> headers
    ) {
        deadLetterService.saveDeadLetter(
                "feed-comment-event-consumer",
                message,
                headers
        );
    }
}