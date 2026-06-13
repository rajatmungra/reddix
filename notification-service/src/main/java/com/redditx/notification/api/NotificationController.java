package com.redditx.notification.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.common.dto.PageResponse;
import com.redditx.notification.application.NotificationService;
import com.redditx.notification.dto.NotificationResponse;
import com.redditx.notification.dto.UnreadCountResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name="unreadOnly", defaultValue = "false") boolean unreadOnly,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "20") int size
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        PageResponse<NotificationResponse> response =
                notificationService.getNotifications(currentUserId, unreadOnly, page, size);

        return ApiResponse.success("Notifications fetched successfully", response);
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        UnreadCountResponse response = notificationService.getUnreadCount(currentUserId);

        return ApiResponse.success("Unread notification count fetched successfully", response);
    }

    @PatchMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("notificationId") UUID notificationId
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        NotificationResponse response =
                notificationService.markRead(currentUserId, notificationId);

        return ApiResponse.success("Notification marked as read", response);
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllRead(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID currentUserId = UUID.fromString(jwt.getSubject());

        notificationService.markAllRead(currentUserId);

        return ApiResponse.success("All notifications marked as read", null);
    }
}