package com.redditx.feed.application;

import com.redditx.feed.domain.DeadLetterEvent;
import com.redditx.feed.infrastructure.DeadLetterEventRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DeadLetterService {

    private final DeadLetterEventRepository deadLetterEventRepository;

    public DeadLetterService(DeadLetterEventRepository deadLetterEventRepository) {
        this.deadLetterEventRepository = deadLetterEventRepository;
    }

    public void saveDeadLetter(
            String consumerName,
            String payload,
            Map<String, Object> headers
    ) {
        String topic = String.valueOf(
                headers.getOrDefault("kafka_receivedTopic", "unknown")
        );

        DeadLetterEvent event = new DeadLetterEvent(
                topic,
                consumerName,
                payload,
                headers.toString()
        );

        deadLetterEventRepository.save(event);
    }
}