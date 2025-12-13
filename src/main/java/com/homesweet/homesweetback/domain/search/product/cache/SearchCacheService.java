package com.homesweet.homesweetback.domain.search.product.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.homesweet.homesweetback.common.util.scroll.SearchScrollResponse;
import com.homesweet.homesweetback.domain.search.product.controller.request.ProductSortType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Elasticsearch 정규화를 활용한 검색 캐싱 서비스
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 13.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchCacheService {

    @Qualifier("searchCacheRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;
    private final KeywordNormalizedService normalizationService;

    private static final String SEARCH_COUNT_PREFIX = "search:count:";
    private static final String SEARCH_CACHE_PREFIX = "search:cache:";
    private static final int CACHE_THRESHOLD = 5;
    private static final Duration COUNTER_TTL = Duration.ofHours(24);
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * 검색 카운트 증가 (Elasticsearch 정규화 적용)
     */
    public boolean incrementSearchCount(String keyword) {
        String normalized = normalizationService.normalizeKeyword(keyword);
        if (normalized == null) {
            return false;
        }

        String countKey = SEARCH_COUNT_PREFIX + normalized;
        Long count = redisTemplate.opsForValue().increment(countKey);

        if (count != null && count == 1) {
            redisTemplate.expire(countKey, COUNTER_TTL);
        }

        log.debug("검색 카운트: [{}] = {}", normalized, count);

        return count != null && count >= CACHE_THRESHOLD;
    }

    /**
     * 캐시 키 생성 (정규화된 키워드 사용)
     */
    public String generateCacheKey(String keyword, Long categoryId,
                                   String sortType, Double minPrice, Double maxPrice) {
        String normalized = normalizationService.normalizeKeyword(keyword);
        if (normalized == null) {
            return null;
        }

        StringBuilder key = new StringBuilder(SEARCH_CACHE_PREFIX);
        key.append(normalized);

        if (categoryId != null) {
            key.append(":cat:").append(categoryId);
        }
        if (sortType != null && !"LATEST".equals(sortType)) {
            key.append(":sort:").append(sortType);
        }
        if (minPrice != null) {
            key.append(":min:").append(minPrice.intValue());
        }
        if (maxPrice != null) {
            key.append(":max:").append(maxPrice.intValue());
        }

        return key.toString();
    }

    /**
     * 캐시 조회
     */
    public <T> SearchScrollResponse<T> getCachedResult(String cacheKey, Class<T> responseType) {
        if (cacheKey == null) {
            return null;
        }

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("✅ 캐시 HIT: {}", cacheKey);
                String json = cached instanceof String ? (String) cached
                        : objectMapper.writeValueAsString(cached);

                return objectMapper.readValue(json,
                        objectMapper.getTypeFactory().constructParametricType(
                                SearchScrollResponse.class, responseType));
            }
        } catch (JsonProcessingException e) {
            log.error("캐시 역직렬화 실패: {}", cacheKey, e);
        }

        log.debug("❌ 캐시 MISS: {}", cacheKey);
        return null;
    }

    /**
     * 캐시 저장
     */
    public <T> void cacheSearchResult(String cacheKey, SearchScrollResponse<T> response) {
        if (cacheKey == null || response == null) {
            return;
        }

        try {
            // 첫 페이지만 캐싱 (limit 제한)
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);

            log.info("💾 캐시 저장: {} ({} items)", cacheKey, response.contents().size());
        } catch (JsonProcessingException e) {
            log.error("캐시 직렬화 실패: {}", cacheKey, e);
        }
    }

    /**
     * 현재 검색 카운트 조회
     */
    public Long getSearchCount(String keyword) {
        String normalized = normalizationService.normalizeKeyword(keyword);
        if (normalized == null) {
            return 0L;
        }

        String countKey = SEARCH_COUNT_PREFIX + normalized;
        Object count = redisTemplate.opsForValue().get(countKey);

        if (count instanceof Number) {
            return ((Number) count).longValue();
        } else if (count instanceof String) {
            return Long.parseLong((String) count);
        }

        return 0L;
    }

    /**
     * 캐시 무효화
     */
    public void invalidateCache(String cacheKey) {
        if (cacheKey != null) {
            redisTemplate.delete(cacheKey);
            log.info("🗑️ 캐시 삭제: {}", cacheKey);
        }
    }
}