package com.gucci.blog_service.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.gucci.blog_service.post.repository")
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.uris}")
    private String uri;

    // 1) Your custom ObjectMapper
    @Bean
    public ObjectMapper elasticsearchObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // 2) Wrap it in a JacksonJsonpMapper
    @Bean
    public JacksonJsonpMapper elasticsearchJsonpMapper(ObjectMapper elasticsearchObjectMapper) {
        return new JacksonJsonpMapper(elasticsearchObjectMapper);
    }

    // 3) The low-level RestClient
    @Bean
    public RestClient lowLevelRestClient() {
        return RestClient.builder(HttpHost.create(uri)).build();
    }

    // 4) Finally, the ElasticsearchClient built with your mapper
    @Bean
    public ElasticsearchClient elasticsearchClient(
            RestClient lowLevelRestClient,
            JacksonJsonpMapper elasticsearchJsonpMapper) {
        RestClientTransport transport =
                new RestClientTransport(lowLevelRestClient, elasticsearchJsonpMapper);
        return new ElasticsearchClient(transport);
    }
}