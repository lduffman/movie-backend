package com.courses_polytech.movie_backend.configurations;

import com.courses_polytech.movie_backend.clients.ExternalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ExternalClientProperties.class)
public class ExternalClientConfiguration {

    private final ExternalClientProperties properties;

    @Bean
    public ExternalApi externalApi() {
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(properties.apiKeyHeader(), properties.apiKey())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(ExternalApi.class);
    }
}
