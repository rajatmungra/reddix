package com.redditx.post.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.event.EventEnvelope;
import com.redditx.common.event.EventTopics;
import com.redditx.common.event.EventTypes;
import com.redditx.post.event.PostCreatedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void savePostCreatedEvent(PostCreatedEvent event) {
        try {
            EventEnvelope<PostCreatedEvent> envelope = new EventEnvelope<>(
                    event.eventId(),
                    EventTypes.POST_CREATED,
                    "post-service",
                    "Post",
                    event.postId().toString(),
                    Instant.now(),
                    event
            );

            String payload = objectMapper.writeValueAsString(envelope);

            OutboxEvent outboxEvent = new OutboxEvent(
                    event.postId(),
                    "Post",
                    EventTypes.POST_CREATED,
                    EventTopics.POST_EVENTS,
                    event.postId().toString(),
                    payload
            );

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize PostCreatedEvent", ex);
        }
    }
}