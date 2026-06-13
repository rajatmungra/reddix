package com.redditx.feed.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.event.EventTopics;
import com.redditx.common.event.EventTypes;
import com.redditx.feed.application.DeadLetterService;
import com.redditx.feed.application.FeedService;
import com.redditx.feed.event.VoteChangedEvent;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VoteEventConsumer {

    private final ObjectMapper objectMapper;
    private final FeedService feedService;
    private final DeadLetterService deadLetterService;

    public VoteEventConsumer(
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
            topics = EventTopics.VOTE_EVENTS,
            groupId = "feed-service-group-v4"
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

                feedService.handleVoteChanged(event);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not process vote event", ex);
        }
    }

    @DltHandler
    public void handleDlt(
            String message,
            @Headers Map<String, Object> headers
    ) {
        deadLetterService.saveDeadLetter(
                "feed-vote-event-consumer",
                message,
                headers
        );
    }
}