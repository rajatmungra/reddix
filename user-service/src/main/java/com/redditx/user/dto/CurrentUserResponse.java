package com.redditx.user.dto;

import java.util.List;

public record CurrentUserResponse(
        String userId,
        String username,
        String email,
        List<String> roles
) {
}