package com.redditx.vote.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}