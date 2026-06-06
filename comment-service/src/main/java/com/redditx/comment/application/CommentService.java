package com.redditx.comment.application;

import com.redditx.comment.client.PostServiceClient;
import com.redditx.comment.domain.Comment;
import com.redditx.comment.dto.CommentResponse;
import com.redditx.comment.dto.CreateCommentRequest;
import com.redditx.comment.dto.UpdateCommentRequest;
import com.redditx.comment.infrastructure.CommentRepository;
import com.redditx.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class CommentService {

    private static final int MAX_COMMENT_DEPTH = 5;

    private final CommentRepository commentRepository;
    private final PostServiceClient postServiceClient;

    public CommentService(
            CommentRepository commentRepository,
            PostServiceClient postServiceClient
    ) {
        this.commentRepository = commentRepository;
        this.postServiceClient = postServiceClient;
    }

    public CommentResponse createComment(
            UUID currentUserId,
            CreateCommentRequest request
    ) {
        validatePostExists(request.postId());

        int depth = 0;

        if (request.parentCommentId() != null) {
            Comment parentComment = commentRepository.findByIdAndDeletedFalse(request.parentCommentId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Parent comment not found"
                    ));

            if (!parentComment.getPostId().equals(request.postId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Parent comment does not belong to this post"
                );
            }

            depth = parentComment.getDepth() + 1;

            if (depth > MAX_COMMENT_DEPTH) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Maximum reply depth exceeded"
                );
            }
        }

        Comment comment = new Comment(
                request.postId(),
                currentUserId,
                request.parentCommentId(),
                request.content().trim(),
                depth
        );

        Comment savedComment = commentRepository.save(comment);

        try {
            postServiceClient.incrementCommentCount(request.postId());
        } catch (RestClientException ex) {
            // In the future, Kafka/outbox will make this reliable.
            // For now, do not fail comment creation after DB save.
        }

        return toResponse(savedComment);
    }

    public PageResponse<CommentResponse> getCommentsByPost(
            UUID postId,
            int page,
            int size
    ) {
        validatePostExists(postId);

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<Comment> comments = commentRepository.findByPostIdAndDeletedFalse(
                postId,
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.ASC, "createdAt")
                )
        );

        return toPageResponse(comments);
    }

    public PageResponse<CommentResponse> getMyComments(
            UUID currentUserId,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        Page<Comment> comments = commentRepository.findByAuthorUserIdAndDeletedFalse(
                currentUserId,
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        return toPageResponse(comments);
    }

    public CommentResponse updateComment(
            UUID currentUserId,
            UUID commentId,
            UpdateCommentRequest request
    ) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment not found"
                ));

        if (!comment.isAuthor(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the author can update this comment"
            );
        }

        comment.updateContent(request.content());

        Comment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    public void deleteComment(UUID currentUserId, UUID commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment not found"
                ));

        if (!comment.isAuthor(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the author can delete this comment"
            );
        }

        comment.markDeleted();
        commentRepository.save(comment);

        try {
            postServiceClient.decrementCommentCount(comment.getPostId());
        } catch (RestClientException ex) {
            // Later Kafka/outbox will repair this kind of inconsistency.
        }
    }

    private void validatePostExists(UUID postId) {
        try {
            postServiceClient.getPost(postId);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Post does not exist or post-service is unavailable"
            );
        }
    }

    private PageResponse<CommentResponse> toPageResponse(Page<Comment> comments) {
        return new PageResponse<>(
                comments.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                comments.getNumber(),
                comments.getSize(),
                comments.getTotalElements(),
                comments.getTotalPages(),
                comments.isLast()
        );
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getAuthorUserId(),
                comment.getParentCommentId(),
                comment.getContent(),
                comment.getDepth(),
                comment.getUpvoteCount(),
                comment.getDownvoteCount(),
                comment.getScore(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}