package com.redditx.notification.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "dead_letter_events",
        indexes = {
                @Index(name = "idx_dead_letter_events_topic", columnList = "topic"),
                @Index(name = "idx_dead_letter_events_created_at", columnList = "created_at")
        }
)
public class DeadLetterEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String topic;

    @Column(name = "consumer_name", nullable = false, length = 100)
    private String consumerName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String headers;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DeadLetterEvent() {
    }

    public DeadLetterEvent(
            String topic,
            String consumerName,
            String payload,
            String headers
    ) {
        this.topic = topic;
        this.consumerName = consumerName;
        this.payload = payload;
        this.headers = headers;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}