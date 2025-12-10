package com.homesweet.homesweetback.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.http.entity.ContentType;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Value("${ELASTIC_HOST}")
    private String host;

    @Value("${ELASTIC_PORT:443}")
    private int port;

    private final ObjectMapper objectMapper;

    public OpenSearchConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {

        RestClient restClient = RestClient.builder(new HttpHost(host, port, "https"))
                // 기본 Content-Type 강제 지정 (406 에러 방지)
                .setDefaultHeaders(new BasicHeader[]{
                        new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
                })
                // AWS OpenSearch에서 요구하는 헤더 추가
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.addInterceptorLast((response, context) -> {
                            response.addHeader("X-Elastic-Product", "Elasticsearch");
                        })
                )
                .build();

        // Transport 생성
        ElasticsearchTransport transport = new RestClientTransport(
                restClient,
                new JacksonJsonpMapper(objectMapper)
        );

        // ElasticsearchClient 반환
        return new ElasticsearchClient(transport);
    }
}