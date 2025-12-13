package com.homesweet.homesweetback.domain.search.product.cache;

import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 검색 키워드 정규화 서비스
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 13.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordNormalizedService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 검색어 정규화 (Elasticsearch Analyze API 활용)
     * - 소문자 변환
     * - 불용어 제거
     * - 형태소 분석
     * - 공백 정규화
     *
     * @param keyword 원본 검색어
     * @return 정규화된 검색어
     */
    public String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        try {
            // Elasticsearch의 analyze API를 통해 검색어 분석
            var analyzeRequest = new co.elastic.clients.elasticsearch.indices.AnalyzeRequest.Builder()
                    .index("products") // 인덱스 이름
                    .text(keyword)
                    .analyzer("keyword_normalizer") // 정규화 전용 analyzer
                    .build();

            var response = elasticsearchTemplate.execute(client ->
                    client.indices().analyze(analyzeRequest)
            );

            // 토큰 추출 및 조합
            List<String> tokens = response.tokens().stream()
                    .map(AnalyzeToken::token)
                    .filter(token -> !token.isBlank())
                    .toList();

            String normalized = String.join(" ", tokens);

            log.debug("검색어 정규화: [{}] -> [{}]", keyword, normalized);

            return normalized.isBlank() ? keyword.toLowerCase().trim() : normalized;

        } catch (Exception e) {
            log.error("검색어 정규화 실패: {}", keyword, e);
            // fallback: 기본 정규화
            return keyword.toLowerCase().trim().replaceAll("\\s+", " ");
        }
    }
}