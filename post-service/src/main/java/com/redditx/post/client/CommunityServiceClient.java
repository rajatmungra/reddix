package com.redditx.post.client;

import com.redditx.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CommunityServiceClient {

    private final RestClient restClient;
    private final String internalSecret;

    public CommunityServiceClient(
            @Qualifier("loadBalancedRestClientBuilder") RestClient.Builder restClientBuilder,
            @Value("${app.internal.secret}") String internalSecret
    ) {
        this.restClient = restClientBuilder.build();
        this.internalSecret = internalSecret;
    }

    public CommunityResponse getCommunityByName(String communityName) {
        try {
            ApiResponse<CommunityResponse> response = restClient.get()
                    .uri("http://community-service/internal/communities/{name}", communityName)
                    .header("X-Internal-Secret", internalSecret)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<CommunityResponse>>() {
                    });

            if (response == null || response.data() == null) {
                throw new RestClientException("Empty response from community-service");
            }

            return response.data();
        } catch (RestClientException ex) {
            throw ex;
        }
    }
}