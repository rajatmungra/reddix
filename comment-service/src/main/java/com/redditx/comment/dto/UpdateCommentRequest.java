package com.redditx.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(

        @NotBlank(message = "Comment content is required")
        @Size(min = 1, max = 5000, message = "Comment content must be between 1 and 5000 characters")
        String content
) {
}