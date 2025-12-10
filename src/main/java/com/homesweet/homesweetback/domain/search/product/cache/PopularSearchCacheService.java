package com.homesweet.homesweetback.domain.search.product.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homesweet.homesweetback.common.util.scroll.SearchScrollResponse;
import com.homesweet.homesweetback.domain.search.product.controller.request.ProductSortType;
import com.homesweet.homesweetback.domain.search.product.controller.response.ProductPreviewResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * 인기 검색어 캐싱 서비스
 * - 검색 빈도 추적
 * - 불용어 처리
 * - 인기 검색 결과 캐싱
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularSearchCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 캐싱 임계값: 5회 이상 검색 시 캐싱
    private static final int CACHE_THRESHOLD = 5;

    // 검색 빈도 카운터 TTL: 24시간
    private static final Duration COUNTER_TTL = Duration.ofHours(24);

    // 캐싱된 검색 결과 TTL: 1시간
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    // Redis Key Prefix
    private static final String SEARCH_COUNT_PREFIX = "search:count:";
    private static final String SEARCH_CACHE_PREFIX = "search:cache:";

    // 한국어 불용어 리스트
    private static final Set<String> STOP_WORDS = Set.of(
            "의", "가", "이", "은", "들", "는", "좀", "잘", "걍", "도", "으로", "자",
            "에", "와", "한", "하다", "을", "를", "인", "듯", "과", "네", "듯이", "지",
            "및", "그", "저", "것", "등", "더", "very", "more", "most", "the", "a", "an"
    );

    /**
     * 불용어 제거 및 정규화된 검색어 생성
     */
    public String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        // 소문자 변환 및 공백 정규화
        String normalized = keyword.toLowerCase().trim().replaceAll("\\s+", " ");

        // 불용어 제거
        String[] words = normalized.split("\\s+");
        List<String> filteredWords = new ArrayList<>();

        for (String word : words) {
            if (!STOP_WORDS.contains(word) && !word.isBlank()) {
                filteredWords.add(word);
            }
        }

        // 불용어 제거 후 남은 단어가 없으면 원본 반환
        if (filteredWords.isEmpty()) {
            return normalized;
        }

        return String.join(" ", filteredWords);
    }

    /**
     * 검색 카운트 증가 및 캐싱 가능 여부 확인
     * @return 캐싱 가능 여부 (5회 이상이면 true)
     */
    public boolean incrementSearchCount(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null) {
            return false;
        }

        String countKey = SEARCH_COUNT_PREFIX + normalizedKeyword;
        Long count = redisTemplate.opsForValue().increment(countKey);

        // 첫 검색이면 TTL 설정
        if (count != null && count == 1) {
            redisTemplate.expire(countKey, COUNTER_TTL);
        }

        log.debug("검색 카운트 증가: {} = {}", normalizedKeyword, count);

        return count != null && count >= CACHE_THRESHOLD;
    }

    /**
     * 캐시 키 생성 (검색 조건 포함)
     */
    public String generateCacheKey(String keyword, Long categoryId, ProductSortType sortType,
                                   Double minPrice, Double maxPrice, List<String> optionFilters) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null) {
            return null;
        }

        StringBuilder keyBuilder = new StringBuilder(SEARCH_CACHE_PREFIX);
        keyBuilder.append(normalizedKeyword);

        if (categoryId != null) {
            keyBuilder.append(":cat:").append(categoryId);
        }
        if (sortType != null && sortType != ProductSortType.LATEST) {
            keyBuilder.append(":sort:").append(sortType.name());
        }
        if (minPrice != null) {
            keyBuilder.append(":min:").append(minPrice);
        }
        if (maxPrice != null) {
            keyBuilder.append(":max:").append(maxPrice);
        }
        if (optionFilters != null && !optionFilters.isEmpty()) {
            keyBuilder.append(":opts:").append(String.join(",", optionFilters));
        }

        return keyBuilder.toString();
    }

    /**
     * 캐시에서 검색 결과 조회
     */
    public SearchScrollResponse<ProductPreviewResponse> getCachedResult(String cacheKey) {
        if (cacheKey == null) {
            return null;
        }

        try {
            Object raw = redisTemplate.opsForValue().get(cacheKey);

            String cached = raw != null ? raw.toString() : null;
            if (cached != null) {
                log.info("캐시 히트: {}", cacheKey);
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructParametricType(
                                SearchScrollResponse.class, ProductPreviewResponse.class));
            }
        } catch (JsonProcessingException e) {
            log.error("캐시 역직렬화 실패: {}", cacheKey, e);
        }

        return null;
    }

    /**
     * 검색 결과 캐싱 (첫 페이지 12개만)
     */
    /**
     * 검색 결과 캐싱 (첫 페이지 12개만)
     */
    public void cacheSearchResult(String cacheKey, SearchScrollResponse<ProductPreviewResponse> response) {
        if (cacheKey == null || response == null) {
            return;
        }

        try {
            // 첫 페이지 12개만 캐싱
            List<ProductPreviewResponse> limitedData = response.contents().stream()
                    .limit(12)
                    .toList();

            // 새로운 응답 객체 생성 (12개로 제한)
            SearchScrollResponse<ProductPreviewResponse> cachedResponse =
                    new SearchScrollResponse<>(limitedData, response.nextCursor(), response.hasNext());

            String json = objectMapper.writeValueAsString(cachedResponse);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);

            log.info("검색 결과 캐싱 완료: {} ({}개 상품)", cacheKey, limitedData.size());
        } catch (JsonProcessingException e) {
            log.error("캐시 직렬화 실패: {}", cacheKey, e);
        }
    }

    /**
     * 현재 검색 카운트 조회
     */
    public Long getSearchCount(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null) {
            return 0L;
        }

        String countKey = SEARCH_COUNT_PREFIX + normalizedKeyword;
        Object countObj = redisTemplate.opsForValue().get(countKey);

        if (countObj == null) {
            return 0L;
        }

        // Object를 Long으로 변환
        if (countObj instanceof Number) {
            return ((Number) countObj).longValue();
        } else if (countObj instanceof String) {
            return Long.parseLong((String) countObj);
        }

        return 0L;
    }
}