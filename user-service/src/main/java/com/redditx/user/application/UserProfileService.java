package com.redditx.user.application;

import com.redditx.user.domain.UserProfile;
import com.redditx.user.dto.CreateUserProfileRequest;
import com.redditx.user.dto.UpdateUserProfileRequest;
import com.redditx.user.dto.UserProfileResponse;
import com.redditx.user.infrastructure.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfileResponse createProfile(CreateUserProfileRequest request) {
        String username = request.username().toLowerCase();
        String email = request.email().toLowerCase();

        if (userProfileRepository.existsByAuthUserId(request.authUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists");
        }

        if (userProfileRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        if (userProfileRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserProfile profile = new UserProfile(
                request.authUserId(),
                username,
                email
        );

        UserProfile savedProfile = userProfileRepository.save(profile);

        return toResponse(savedProfile);
    }

    public UserProfileResponse getCurrentUserProfile(UUID authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found"
                ));

        return toResponse(profile);
    }

    public UserProfileResponse updateCurrentUserProfile(
            UUID authUserId,
            UpdateUserProfileRequest request
    ) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found"
                ));

        profile.updateProfile(
                request.displayName(),
                request.bio(),
                request.avatarUrl()
        );

        UserProfile savedProfile = userProfileRepository.save(profile);

        return toResponse(savedProfile);
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getAuthUserId(),
                profile.getUsername(),
                profile.getEmail(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getPostKarma(),
                profile.getCommentKarma(),
                profile.getTotalKarma(),
                profile.getCreatedAt()
        );
    }
}