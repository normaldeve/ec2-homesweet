package com.homesweet.homesweetback.domain.search.product.service.impl;

import com.homesweet.homesweetback.common.util.scroll.CursorUtil;
import com.homesweet.homesweetback.common.util.scroll.SearchScrollResponse;
import com.homesweet.homesweetback.domain.search.log.event.SearchLogEvent;
import com.homesweet.homesweetback.domain.search.log.service.SearchLogService;
import com.homesweet.homesweetback.domain.search.product.cache.KeywordNormalizedService;
import com.homesweet.homesweetback.domain.search.product.cache.SearchCacheService;
import com.homesweet.homesweetback.domain.search.product.controller.request.ProductSortType;
import com.homesweet.homesweetback.domain.search.product.controller.response.ProductPreviewResponse;
import com.homesweet.homesweetback.domain.search.product.repository.ProductSearchRepository;
import com.homesweet.homesweetback.domain.search.product.repository.document.ProductDocument;
import com.homesweet.homesweetback.domain.search.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 상품 검색 서비스 (정규화 + 캐싱 + 로깅)
 *
 * @author junnukim1007gmail.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductSearchRepository productSearchRepository;
    private final CursorUtil cursorUtil;
    private final KeywordNormalizedService normalizationService;
    private final SearchCacheService cacheService;
    private final SearchLogService searchLogService;

    @Override
    public List<String> autocomplete(String keyword) {
        return productSearchRepository.autocomplete(keyword);
    }

    @Override
    public SearchScrollResponse<ProductPreviewResponse> searchProducts(
            String cursor, Long categoryId, String keyword, ProductSortType sortType,
            Double minPrice, Double maxPrice, int limit, List<String> optionFilters) {

        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;
        SearchScrollResponse<ProductPreviewResponse> result;

        // 첫 페이지 + 키워드 검색 시 캐싱 로직
        if (cursor == null && keyword != null && !keyword.isBlank()) {

            // 검색 카운트 증가
            boolean shouldCache = cacheService.incrementSearchCount(keyword);

            // 캐시 키 생성
            String cacheKey = cacheService.generateCacheKey(
                    keyword, categoryId, sortType.name(), minPrice, maxPrice);

            // 캐시 조회
            if (shouldCache && cacheKey != null) {
                result = cacheService.getCachedResult(cacheKey, ProductPreviewResponse.class);

                if (result != null) {
                    cacheHit = true;
                    logSearchEvent(keyword, categoryId, sortType, minPrice, maxPrice,
                            optionFilters, cursor, limit, result,
                            System.currentTimeMillis() - startTime, cacheHit);
                    return result;
                }

                // 캐시 미스 - 검색 실행 후 캐싱
                result = executeSearch(cursor, categoryId, keyword, sortType,
                        minPrice, maxPrice, limit, optionFilters);
                cacheService.cacheSearchResult(cacheKey, result);

                logSearchEvent(keyword, categoryId, sortType, minPrice, maxPrice,
                        optionFilters, cursor, limit, result,
                        System.currentTimeMillis() - startTime, cacheHit);
                return result;
            }
        }

        // 일반 검색
        result = executeSearch(cursor, categoryId, keyword, sortType,
                minPrice, maxPrice, limit, optionFilters);

        logSearchEvent(keyword, categoryId, sortType, minPrice, maxPrice,
                optionFilters, cursor, limit, result,
                System.currentTimeMillis() - startTime, cacheHit);

        return result;
    }

    private SearchScrollResponse<ProductPreviewResponse> executeSearch(
            String cursor, Long categoryId, String keyword, ProductSortType sortType,
            Double minPrice, Double maxPrice, int limit, List<String> optionFilters) {

        SearchHits<ProductDocument> hits = productSearchRepository.search(
                cursor, categoryId, limit, keyword, sortType, minPrice, maxPrice, optionFilters);

        List<ProductDocument> docs = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        boolean hasNext = docs.size() > limit;
        List<ProductDocument> result = hasNext ? docs.subList(0, limit) : docs;
        ProductDocument lastDoc = hasNext ? result.getLast() : null;
        Float lastScore = hasNext ? hits.getSearchHits().get(limit - 1).getScore() : null;

        List<Object> sortValues = lastDoc != null ? switch (sortType) {
            case RECOMMENDED -> List.of(lastScore, lastDoc.getProductId());
            case LATEST -> List.of(
                    lastDoc.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    lastDoc.getProductId());
            case PRICE_LOW, PRICE_HIGH -> List.of(lastDoc.getBasePrice(), lastDoc.getProductId());
            case POPULAR -> List.of(
                    lastDoc.getAverageRating() != null ? lastDoc.getAverageRating() : 0.0,
                    lastDoc.getReviewCount() != null ? lastDoc.getReviewCount() : 0,
                    lastDoc.getProductId());
        } : null;

        String nextCursor = cursorUtil.encode(sortValues);

        List<ProductPreviewResponse> responses = result.stream()
                .map(ProductPreviewResponse::fromDocument)
                .toList();

        return SearchScrollResponse.of(responses, nextCursor, hasNext);
    }

    /**
     * 검색 로그 이벤트 발행
     */
    private void logSearchEvent(String keyword, Long categoryId, ProductSortType sortType,
                                Double minPrice, Double maxPrice, List<String> optionFilters,
                                String cursor, int limit,
                                SearchScrollResponse<ProductPreviewResponse> result,
                                long duration, boolean cacheHit) {

        String normalizedKeyword = keyword != null ?
                normalizationService.normalizeKeyword(keyword) : null;

        SearchLogEvent event = searchLogService.createLogEventBuilder("PRODUCT")
                .originalKeyword(keyword)
                .normalizedKeyword(normalizedKeyword)
                .categoryId(categoryId)
                .sortType(sortType.name())
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .optionFilters(optionFilters)
                .resultCount(result.contents().size())
                .searchDuration(duration)
                .cacheHit(cacheHit)
                .cursor(cursor)
                .limit(limit)
                .hasNext(result.hasNext())
                .build();

        searchLogService.logSearch(event);
    }
}