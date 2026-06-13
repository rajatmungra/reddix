package com.redditx.common.event;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String sourceService,
        String aggregateType,
        String aggregateId,
        Instant occurredAt,
        T payload
) {
}