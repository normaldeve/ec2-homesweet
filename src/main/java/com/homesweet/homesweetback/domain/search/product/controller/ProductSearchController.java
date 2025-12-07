package com.homesweet.homesweetback.domain.search.product.controller;

import com.homesweet.homesweetback.common.util.scroll.SearchScrollResponse;
import com.homesweet.homesweetback.domain.search.product.controller.request.ProductSortType;
import com.homesweet.homesweetback.domain.search.product.controller.response.ProductPreviewResponse;
import com.homesweet.homesweetback.domain.search.product.service.ProductSearchService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품 검색 컨트롤러
 *
 * @author junnukim1007gmail.com
 * @date 25. 11. 24.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search/products")
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    /**
     * 인증 사용자 상품 조회 및 검색
     *
     */
    @GetMapping
    public ResponseEntity<SearchScrollResponse<ProductPreviewResponse>> searchProducts(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") ProductSortType sortType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false, name = "optionFilters") List<String> optionFilters
    ) {

        SearchScrollResponse<ProductPreviewResponse> result = productSearchService.searchProducts(nextCursor, categoryId, keyword, sortType, minPrice, maxPrice, limit, optionFilters);

        return ResponseEntity.ok(result);
    }

    /**
     * 검색어 자동 완성 API
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@NotNull @RequestParam String keyword) {
        List<String> result = productSearchService.autocomplete(keyword);
        return ResponseEntity.ok(result);
    }

    /**
     * 비인증 사용자 상품 조회 및 검색
     */
    @GetMapping("/previews")
    public ResponseEntity<SearchScrollResponse<ProductPreviewResponse>> getProductPreviews(
            @RequestParam(required = false) String nextCursor,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "LATEST") ProductSortType sortType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false, name = "optionFilters") List<String> optionFilters
    ) {

        SearchScrollResponse<ProductPreviewResponse> response =
                productSearchService.searchProducts(nextCursor, categoryId, keyword, sortType, minPrice, maxPrice, limit, optionFilters);

        return ResponseEntity.ok(response);
    }
}
