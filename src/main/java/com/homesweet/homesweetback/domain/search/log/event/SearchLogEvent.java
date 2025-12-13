package com.homesweet.homesweetback.domain.search.log.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 검색 로그 이벤트
 * Elasticsearch에 검색 로그를 저장하여 Kibana에서 분석
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 13.
 */
@Getter
@Builder
public class SearchLogEvent {

    // 기본 정보
    private String searchId;
    private LocalDateTime timestamp;
    private String searchType; // PRODUCT, COMMUNITY, CHATROOM

    // 검색 조건
    private String originalKeyword;
    private String normalizedKeyword;
    private Long categoryId;
    private String sortType;
    private Double minPrice;
    private Double maxPrice;
    private List<String> optionFilters;

    // 검색 결과
    private Integer resultCount;
    private Long searchDuration;
    private Boolean cacheHit;
    private Float maxScore;

    // 사용자 정보
    private Long userId;
    private String sessionId;
    private String ipAddress;
    private String userAgent;

    // 페이징 정보
    private String cursor;
    private Integer limit;
    private Boolean hasNext;
}