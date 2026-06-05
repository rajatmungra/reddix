package com.redditx.auth.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserServiceClient {

    private final RestClient restClient;
    private final String internalSecret;

    public UserServiceClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
    }

    public void createUserProfile(CreateUserProfileRequest request) {
        restClient.post()
                .uri("http://user-service/internal/users/profiles")
                .header("X-Internal-Secret", internalSecret)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}