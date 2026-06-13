package com.redditx.vote.outbox;

import com.redditx.vote.outbox.OutboxEvent;
import com.redditx.vote.outbox.OutboxStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisher {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                PageRequest.of(0, BATCH_SIZE)
        );

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {
        try {
            kafkaTemplate
                    .send(event.getTopic(), event.getEventKey(), event.getPayload())
                    .get();

            event.markPublished();
            outboxEventRepository.save(event);
        } catch (Exception ex) {
            event.markFailed(ex.getMessage());
            outboxEventRepository.save(event);
        }
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void retryFailedEvents() {
        List<OutboxEvent> failedEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.FAILED,
                PageRequest.of(0, BATCH_SIZE)
        );

        for (OutboxEvent event : failedEvents) {
            if (event.getRetryCount() < MAX_RETRY_COUNT) {
                event.markPendingAgain();
                outboxEventRepository.save(event);
            }
        }
    }
}