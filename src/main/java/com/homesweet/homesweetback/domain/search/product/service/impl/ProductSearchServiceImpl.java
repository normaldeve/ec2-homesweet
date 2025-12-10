package com.homesweet.homesweetback.domain.search.product.service.impl;

import com.homesweet.homesweetback.common.util.scroll.CursorUtil;
import com.homesweet.homesweetback.common.util.scroll.SearchScrollResponse;
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
 * 상품 검색 서비스 구현체 (인기 검색어 캐싱 적용)
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
//    private final PopularSearchCacheService cacheService;

    @Override
    public List<String> autocomplete(String keyword) {
        return productSearchRepository.autocomplete(keyword);
    }

    /**
     * [인증] 사용자 상품 검색 및 조회 (캐싱 적용)
     */
    @Override
    public SearchScrollResponse<ProductPreviewResponse> searchProducts(
            String cursor, Long categoryId, String keyword, ProductSortType sortType,
            Double minPrice, Double maxPrice, int limit, List<String> optionFilters) {

        // 첫 페이지이고 키워드가 있는 경우에만 캐싱 로직 적용
//        if (cursor == null && keyword != null && !keyword.isBlank()) {
//
//            // 1. 검색 카운트 증가
//            boolean shouldCache = cacheService.incrementSearchCount(keyword);
//
//            // 2. 캐시 키 생성
//            String cacheKey = cacheService.generateCacheKey(
//                    keyword, categoryId, sortType, minPrice, maxPrice, optionFilters);
//
//            // 3. 캐시에서 조회 시도 (5회 이상 검색된 경우)
//            if (shouldCache && cacheKey != null) {
//                SearchScrollResponse<ProductPreviewResponse> cachedResult =
//                        cacheService.getCachedResult(cacheKey);
//
//                if (cachedResult != null) {
//                    log.info("캐시된 검색 결과 반환: {}", keyword);
//                    return cachedResult;
//                }
//
//                // 4. 캐시 미스 - 검색 실행 후 캐싱
//                log.info("캐시 미스 - 검색 실행 및 캐싱: {}", keyword);
//                SearchScrollResponse<ProductPreviewResponse> result =
//                        executeSearch(cursor, categoryId, keyword, sortType, minPrice, maxPrice, 12, optionFilters);
//
//                cacheService.cacheSearchResult(cacheKey, result);
//                return result;
//            }
//
//            // 5. 아직 캐싱 임계값 미달 - 일반 검색 실행
//            Long currentCount = cacheService.getSearchCount(keyword);
//            log.debug("검색 실행 (캐싱 미달: {}/5): {}", currentCount, keyword);
//        }

        // 일반 검색 실행 (페이징, 비키워드 검색 등)
        return executeSearch(cursor, categoryId, keyword, sortType, minPrice, maxPrice, limit, optionFilters);
    }

    private SearchScrollResponse<ProductPreviewResponse> executeSearch(
            String cursor,
            Long categoryId,
            String keyword,
            ProductSortType sortType,
            Double minPrice,
            Double maxPrice,
            int limit,
            List<String> optionFilters
    ) {

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
            case LATEST -> List.of(lastDoc.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    lastDoc.getProductId());
            case PRICE_LOW, PRICE_HIGH ->
                    List.of(lastDoc.getBasePrice(), lastDoc.getProductId());
            case POPULAR -> List.of(
                    lastDoc.getAverageRating() != null ? lastDoc.getAverageRating() : 0.0,
                    lastDoc.getReviewCount() != null ? lastDoc.getReviewCount() : 0,
                    lastDoc.getProductId()
            );
        } : null;

        String nextCursor = cursorUtil.encode(sortValues);

        List<ProductPreviewResponse> responses = result.stream()
                .map(ProductPreviewResponse::fromDocument)
                .toList();

        return SearchScrollResponse.of(responses, nextCursor, hasNext);
    }
}