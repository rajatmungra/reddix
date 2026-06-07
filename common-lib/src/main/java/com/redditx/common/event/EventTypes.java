package com.redditx.common.event;

public final class EventTypes {

    private EventTypes() {
    }

    public static final String POST_CREATED = "PostCreatedEvent";
    public static final String POST_UPDATED = "PostUpdatedEvent";
    public static final String POST_DELETED = "PostDeletedEvent";

    public static final String COMMENT_CREATED = "CommentCreatedEvent";
    public static final String COMMENT_DELETED = "CommentDeletedEvent";

    public static final String VOTE_CHANGED = "VoteChangedEvent";

    public static final String USER_REGISTERED = "UserRegisteredEvent";
}