package com.redditx.auth.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String email,
        String role,
        String accessToken,
        String tokenType
) {
}