package com.redditx.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @Size(min = 3, max = 100, message = "Display name must be between 3 and 100 characters")
        String displayName,

        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio,

        @Size(max = 1000, message = "Avatar URL cannot exceed 1000 characters")
        String avatarUrl
) {
}