package com.redditx.comment.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.comment.event.CommentCreatedEvent;
import com.redditx.comment.event.CommentDeletedEvent;
import com.redditx.common.event.EventEnvelope;
import com.redditx.common.event.EventTopics;
import com.redditx.common.event.EventTypes;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class CommentOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public CommentOutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void saveCommentCreatedEvent(CommentCreatedEvent event) {
        saveEvent(
                event.eventId(),
                event.commentId(),
                "Comment",
                EventTypes.COMMENT_CREATED,
                EventTopics.COMMENT_EVENTS,
                event.commentId().toString(),
                event
        );
    }

    public void saveCommentDeletedEvent(CommentDeletedEvent event) {
        saveEvent(
                event.eventId(),
                event.commentId(),
                "Comment",
                EventTypes.COMMENT_DELETED,
                EventTopics.COMMENT_EVENTS,
                event.commentId().toString(),
                event
        );
    }

    private void saveEvent(
            UUID eventId,
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String topic,
            String eventKey,
            Object eventPayload
    ) {
        try {
            EventEnvelope<Object> envelope = new EventEnvelope<>(
                    eventId,
                    eventType,
                    "comment-service",
                    aggregateType,
                    aggregateId.toString(),
                    Instant.now(),
                    eventPayload
            );

            String payload = objectMapper.writeValueAsString(envelope);

            OutboxEvent outboxEvent = new OutboxEvent(
                    aggregateId,
                    aggregateType,
                    eventType,
                    topic,
                    eventKey,
                    payload
            );

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize comment event", ex);
        }
    }
}