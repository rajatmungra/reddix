package com.redditx.comment.outbox;

public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}