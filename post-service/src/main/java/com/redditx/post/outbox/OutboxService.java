package com.redditx.post.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.event.EventTopics;
import com.redditx.common.event.EventTypes;
import com.redditx.post.event.PostCreatedEvent;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
            String payload = objectMapper.writeValueAsString(event);

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