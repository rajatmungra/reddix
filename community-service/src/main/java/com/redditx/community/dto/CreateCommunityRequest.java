package com.redditx.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCommunityRequest(

        @NotBlank(message = "Community name is required")
        @Size(min = 3, max = 50, message = "Community name must be between 3 and 50 characters")
        @Pattern(
                regexp = "^[a-zA-Z0-9_-]+$",
                message = "Community name can only contain letters, numbers, underscore and hyphen"
        )
        String name,

        @NotBlank(message = "Display name is required")
        @Size(min = 3, max = 100, message = "Display name must be between 3 and 100 characters")
        String displayName,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description
) {
}