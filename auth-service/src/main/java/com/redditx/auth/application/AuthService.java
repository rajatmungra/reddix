package com.redditx.auth.application;

import com.redditx.auth.client.CreateUserProfileRequest;
import com.redditx.auth.client.UserServiceClient;
import com.redditx.auth.domain.AccountStatus;
import com.redditx.auth.domain.AuthUser;
import com.redditx.auth.dto.AuthResponse;
import com.redditx.auth.dto.LoginRequest;
import com.redditx.auth.dto.RegisterRequest;
import com.redditx.auth.infrastructure.AuthUserRepository;
import com.redditx.auth.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserServiceClient userServiceClient;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UserServiceClient userServiceClient
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userServiceClient = userServiceClient;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        String username = request.username().toLowerCase();

        if (authUserRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        if (authUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        String passwordHash = passwordEncoder.encode(request.password());

        AuthUser user = new AuthUser(username, email, passwordHash);
        AuthUser savedUser = authUserRepository.save(user);

        try {
            userServiceClient.createUserProfile(
                    new CreateUserProfileRequest(
                            savedUser.getId(),
                            savedUser.getUsername(),
                            savedUser.getEmail()
                    )
            );
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User profile service is unavailable. Please try again."
            );
        }

        String token = jwtService.generateAccessToken(savedUser);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                token,
                "Bearer"
        );
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();

        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateAccessToken(user);

        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                token,
                "Bearer"
        );
    }
}