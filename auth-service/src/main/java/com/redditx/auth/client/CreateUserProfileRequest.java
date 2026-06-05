package com.redditx.auth.client;

import java.util.UUID;

public record CreateUserProfileRequest(
        UUID authUserId,
        String username,
        String email
) {
}