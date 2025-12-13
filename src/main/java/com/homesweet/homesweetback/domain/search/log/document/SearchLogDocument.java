package com.homesweet.homesweetback.domain.search.log.document;

import com.homesweet.homesweetback.domain.search.log.event.SearchLogEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 검색 로그 Document (Kibana 분석용)
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 13.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "search_logs")
@Setting(settingPath = "/elasticsearch/search-log-settings.json")
public class SearchLogDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String searchId;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime timestamp;

    @Field(type = FieldType.Keyword, name = "search_type")
    private String searchType;

    // 검색 조건
    @Field(type = FieldType.Text, name = "original_keyword")
    private String originalKeyword;

    @Field(type = FieldType.Keyword, name = "normalized_keyword")
    private String normalizedKeyword;

    @Field(type = FieldType.Long, name = "category_id")
    private Long categoryId;

    @Field(type = FieldType.Keyword, name = "sort_type")
    private String sortType;

    @Field(type = FieldType.Double, name = "min_price")
    private Double minPrice;

    @Field(type = FieldType.Double, name = "max_price")
    private Double maxPrice;

    @Field(type = FieldType.Keyword, name = "option_filters")
    private List<String> optionFilters;

    // 검색 결과
    @Field(type = FieldType.Integer, name = "result_count")
    private Integer resultCount;

    @Field(type = FieldType.Long, name = "search_duration")
    private Long searchDuration;

    @Field(type = FieldType.Boolean, name = "cache_hit")
    private Boolean cacheHit;

    @Field(type = FieldType.Float, name = "max_score")
    private Float maxScore;

    // 사용자 정보
    @Field(type = FieldType.Long, name = "user_id")
    private Long userId;

    @Field(type = FieldType.Keyword, name = "session_id")
    private String sessionId;

    @Field(type = FieldType.Ip, name = "ip_address")
    private String ipAddress;

    @Field(type = FieldType.Text, name = "user_agent")
    private String userAgent;

    // 페이징 정보
    @Field(type = FieldType.Keyword)
    private String cursor;

    @Field(type = FieldType.Integer)
    private Integer limit;

    @Field(type = FieldType.Boolean, name = "has_next")
    private Boolean hasNext;

    public static SearchLogDocument from(SearchLogEvent event) {
        return SearchLogDocument.builder()
                .searchId(event.getSearchId())
                .timestamp(event.getTimestamp())
                .searchType(event.getSearchType())
                .originalKeyword(event.getOriginalKeyword())
                .normalizedKeyword(event.getNormalizedKeyword())
                .categoryId(event.getCategoryId())
                .sortType(event.getSortType())
                .minPrice(event.getMinPrice())
                .maxPrice(event.getMaxPrice())
                .optionFilters(event.getOptionFilters())
                .resultCount(event.getResultCount())
                .searchDuration(event.getSearchDuration())
                .cacheHit(event.getCacheHit())
                .maxScore(event.getMaxScore())
                .userId(event.getUserId())
                .sessionId(event.getSessionId())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .cursor(event.getCursor())
                .limit(event.getLimit())
                .hasNext(event.getHasNext())
                .build();
    }
}