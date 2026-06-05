package com.redditx.user.api;

import com.redditx.common.dto.ApiResponse;
import com.redditx.user.application.UserProfileService;
import com.redditx.user.dto.CreateUserProfileRequest;
import com.redditx.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserProfileService userProfileService;
    private final String internalSecret;

    public InternalUserController(
            UserProfileService userProfileService,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.userProfileService = userProfileService;
        this.internalSecret = internalSecret;
    }

    @PostMapping("/profiles")
    public ApiResponse<UserProfileResponse> createProfile(
            @RequestHeader("X-Internal-Secret") String requestSecret,
            @Valid @RequestBody CreateUserProfileRequest request
    ) {
        if (!internalSecret.equals(requestSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal secret");
        }

        UserProfileResponse response = userProfileService.createProfile(request);

        return ApiResponse.success("User profile created successfully", response);
    }
}