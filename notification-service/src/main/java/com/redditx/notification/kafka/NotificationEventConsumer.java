package com.redditx.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.event.EventTopics;
import com.redditx.common.event.EventTypes;
import com.redditx.notification.application.DeadLetterService;
import com.redditx.notification.application.NotificationService;
import com.redditx.notification.event.CommentCreatedEvent;
import com.redditx.notification.event.VoteChangedEvent;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final DeadLetterService deadLetterService;

    public NotificationEventConsumer(
            ObjectMapper objectMapper,
            NotificationService notificationService,
            DeadLetterService deadLetterService
    ) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
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
            groupId = "notification-service-group-v3"
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

                notificationService.handleCommentCreated(event);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not process comment notification event", ex);
        }
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
            topics = EventTopics.VOTE_EVENTS,
            groupId = "notification-service-group-v3"
    )
    public void consumeVoteEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);

            String eventType = root.get("eventType").asText();
            JsonNode payloadNode = root.get("payload");

            if (EventTypes.VOTE_CHANGED.equals(eventType)) {
                VoteChangedEvent event = objectMapper.treeToValue(
                        payloadNode,
                        VoteChangedEvent.class
                );

                notificationService.handleVoteChanged(event);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not process vote notification event", ex);
        }
    }

    @DltHandler
    public void handleDlt(
            String message,
            @Headers Map<String, Object> headers
    ) {
        deadLetterService.saveDeadLetter(
                "notification-event-consumer",
                message,
                headers
        );
    }
}