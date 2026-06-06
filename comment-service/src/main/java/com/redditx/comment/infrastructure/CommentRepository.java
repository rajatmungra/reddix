package com.redditx.comment.infrastructure;

import com.redditx.comment.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    Optional<Comment> findByIdAndDeletedFalse(UUID id);

    Page<Comment> findByPostIdAndDeletedFalse(UUID postId, Pageable pageable);

    Page<Comment> findByAuthorUserIdAndDeletedFalse(UUID authorUserId, Pageable pageable);
}