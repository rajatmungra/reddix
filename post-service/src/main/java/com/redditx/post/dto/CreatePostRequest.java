package com.redditx.post.dto;

import com.redditx.post.domain.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(

        @NotBlank(message = "Community name is required")
        @Size(min = 3, max = 50, message = "Community name must be between 3 and 50 characters")
        String communityName,

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 300, message = "Title must be between 3 and 300 characters")
        String title,

        @Size(max = 10000, message = "Content cannot exceed 10000 characters")
        String content,

        @Size(max = 1000, message = "URL cannot exceed 1000 characters")
        String url,

        @NotNull(message = "Post type is required")
        PostType postType
) {
}