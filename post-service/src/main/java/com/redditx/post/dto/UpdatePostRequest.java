package com.redditx.post.dto;

import jakarta.validation.constraints.Size;

public record UpdatePostRequest(

        @Size(min = 3, max = 300, message = "Title must be between 3 and 300 characters")
        String title,

        @Size(max = 10000, message = "Content cannot exceed 10000 characters")
        String content,

        @Size(max = 1000, message = "URL cannot exceed 1000 characters")
        String url
) {
}