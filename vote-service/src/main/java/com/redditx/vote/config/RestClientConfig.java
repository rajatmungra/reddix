package com.redditx.vote.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestClient.Builder normalRestClientBuilder(
            ObservationRegistry observationRegistry
    ) {
        return RestClient.builder()
                .observationRegistry(observationRegistry);
    }

    @Bean
    @LoadBalanced
    @Qualifier("loadBalancedRestClientBuilder")
    public RestClient.Builder loadBalancedRestClientBuilder(
            ObservationRegistry observationRegistry
    ) {
        return RestClient.builder()
                .observationRegistry(observationRegistry);
    }
}