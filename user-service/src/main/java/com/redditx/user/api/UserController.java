package com.redditx.user.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.user.application.UserProfileService;
import com.redditx.user.dto.UpdateUserProfileRequest;
import com.redditx.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authUserId = UUID.fromString(jwt.getSubject());

        UserProfileResponse response =
                userProfileService.getCurrentUserProfile(authUserId);

        return ApiResponse.success("Current user profile fetched successfully", response);
    }

    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        UUID authUserId = UUID.fromString(jwt.getSubject());

        UserProfileResponse response =
                userProfileService.updateCurrentUserProfile(authUserId, request);

        return ApiResponse.success("User profile updated successfully", response);
    }
}