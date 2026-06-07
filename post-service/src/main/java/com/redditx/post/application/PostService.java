package com.redditx.post.application;

import com.redditx.common.dto.PageResponse;
import com.redditx.post.client.CommunityServiceClient;
import com.redditx.post.domain.Post;
import com.redditx.post.domain.PostType;
import com.redditx.post.dto.CreatePostRequest;
import com.redditx.post.dto.PostResponse;
import com.redditx.post.dto.UpdatePostRequest;
import com.redditx.post.dto.VoteCountUpdateRequest;
import com.redditx.post.event.PostCreatedEvent;
import com.redditx.post.infrastructure.PostRepository;
import com.redditx.post.outbox.OutboxService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommunityServiceClient communityServiceClient;
    private final OutboxService outboxService;

    public PostService(
            PostRepository postRepository,
            CommunityServiceClient communityServiceClient,
            OutboxService outboxService
    ) {
        this.postRepository = postRepository;
        this.communityServiceClient = communityServiceClient;
        this.outboxService = outboxService;
    }

    @Transactional
    public PostResponse createPost(UUID currentUserId, CreatePostRequest request) {
        String communityName = normalizeCommunityName(request.communityName());

        validatePostContent(request);

        try {
            communityServiceClient.getCommunityByName(communityName);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Community does not exist or community-service is unavailable"
            );
        }

        Post post = new Post(
                communityName,
                currentUserId,
                request.title().trim(),
                trimNullable(request.content()),
                trimNullable(request.url()),
                request.postType()
        );

        Post savedPost = postRepository.save(post);

        PostCreatedEvent event = new PostCreatedEvent(
                UUID.randomUUID(),
                savedPost.getId(),
                savedPost.getCommunityName(),
                savedPost.getAuthorUserId(),
                savedPost.getTitle(),
                savedPost.getPostType().name(),
                Instant.now()
        );

        outboxService.savePostCreatedEvent(event);

        return toResponse(savedPost);
    }

    public PostResponse getPost(UUID postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        return toResponse(post);
    }

    public PageResponse<PostResponse> getPostsByCommunity(
            String communityName,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<Post> posts = postRepository.findByCommunityNameAndDeletedFalse(
                normalizeCommunityName(communityName),
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return toPageResponse(posts);
    }

    public PageResponse<PostResponse> getMyPosts(
            UUID currentUserId,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Page<Post> posts = postRepository.findByAuthorUserIdAndDeletedFalse(
                currentUserId,
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return toPageResponse(posts);
    }

    public PostResponse updatePost(
            UUID currentUserId,
            UUID postId,
            UpdatePostRequest request
    ) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        if (!post.isAuthor(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the author can update this post"
            );
        }

        post.update(
                request.title(),
                request.content(),
                request.url()
        );

        validateUpdatedPost(post);

        Post savedPost = postRepository.save(post);

        return toResponse(savedPost);
    }

    public void deletePost(UUID currentUserId, UUID postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        if (!post.isAuthor(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the author can delete this post"
            );
        }

        post.markDeleted();
        postRepository.save(post);
    }

    private void validatePostContent(CreatePostRequest request) {
        if (request.postType() == PostType.TEXT) {
            if (request.content() == null || request.content().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Text post requires content"
                );
            }
        }

        if (request.postType() == PostType.LINK) {
            if (request.url() == null || request.url().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Link post requires URL"
                );
            }
        }
    }

    private void validateUpdatedPost(Post post) {
        if (post.getPostType() == PostType.TEXT) {
            if (post.getContent() == null || post.getContent().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Text post requires content"
                );
            }
        }

        if (post.getPostType() == PostType.LINK) {
            if (post.getUrl() == null || post.getUrl().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Link post requires URL"
                );
            }
        }
    }

    private PageResponse<PostResponse> toPageResponse(Page<Post> posts) {
        return new PageResponse<>(
                posts.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                posts.getNumber(),
                posts.getSize(),
                posts.getTotalElements(),
                posts.getTotalPages(),
                posts.isLast()
        );
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getCommunityName(),
                post.getAuthorUserId(),
                post.getTitle(),
                post.getContent(),
                post.getUrl(),
                post.getPostType(),
                post.getUpvoteCount(),
                post.getDownvoteCount(),
                post.getScore(),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public PostResponse getPostForInternal(UUID postId) {
        return getPost(postId);
    }

    public void incrementCommentCount(UUID postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        post.incrementCommentCount();
        postRepository.save(post);
    }

    public void decrementCommentCount(UUID postId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        post.decrementCommentCount();
        postRepository.save(post);
    }

    public void applyVoteCountDelta(UUID postId, VoteCountUpdateRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        post.applyVoteDelta(request.upvoteDelta(), request.downvoteDelta());

        postRepository.save(post);
    }

    private String normalizeCommunityName(String communityName) {
        return communityName.trim().toLowerCase();
    }

    private String trimNullable(String value) {
        return value == null ? null : value.trim();
    }
}