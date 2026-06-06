package com.redditx.post.infrastructure;

import com.redditx.post.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findByIdAndDeletedFalse(UUID id);

    Page<Post> findByCommunityNameAndDeletedFalse(String communityName, Pageable pageable);

    Page<Post> findByAuthorUserIdAndDeletedFalse(UUID authorUserId, Pageable pageable);
}