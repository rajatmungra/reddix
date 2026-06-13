package com.redditx.vote.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redditx.common.event.EventEnvelope;
import com.redditx.common.event.EventTopics;
import com.redditx.common.event.EventTypes;
import com.redditx.vote.event.VoteChangedEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class VoteOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public VoteOutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void saveVoteChangedEvent(VoteChangedEvent event) {
        try {
            EventEnvelope<VoteChangedEvent> envelope = new EventEnvelope<>(
                    event.eventId(),
                    EventTypes.VOTE_CHANGED,
                    "vote-service",
                    event.targetType(),
                    event.targetId().toString(),
                    Instant.now(),
                    event
            );

            String payload = objectMapper.writeValueAsString(envelope);

            OutboxEvent outboxEvent = new OutboxEvent(
                    event.targetId(),
                    event.targetType(),
                    EventTypes.VOTE_CHANGED,
                    EventTopics.VOTE_EVENTS,
                    event.targetId().toString(),
                    payload
            );

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize VoteChangedEvent", ex);
        }
    }
}