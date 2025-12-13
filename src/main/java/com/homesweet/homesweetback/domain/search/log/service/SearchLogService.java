package com.homesweet.homesweetback.domain.search.log.service;

import com.homesweet.homesweetback.domain.search.log.document.SearchLogDocument;
import com.homesweet.homesweetback.domain.search.log.event.SearchLogEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 검색 로그 서비스
 * 비동기로 Elasticsearch에 검색 로그 저장
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 13.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 검색 로그 비동기 저장
     */
    @Async("productEventExecutor")
    public void logSearch(SearchLogEvent event) {
        try {
            SearchLogDocument document = SearchLogDocument.from(event);
            elasticsearchOperations.save(document);

            log.info("📊 검색 로그 저장: {} - keyword=[{}], results={}, duration={}ms, cache={}",
                    event.getSearchType(),
                    event.getNormalizedKeyword(),
                    event.getResultCount(),
                    event.getSearchDuration(),
                    event.getCacheHit());

        } catch (Exception e) {
            log.error("검색 로그 저장 실패", e);
        }
    }

    /**
     * SearchLogEvent 빌더 헬퍼
     */
    public SearchLogEvent.SearchLogEventBuilder createLogEventBuilder(String searchType) {
        HttpServletRequest request = getCurrentRequest();

        return SearchLogEvent.builder()
                .searchId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .searchType(searchType)
                .sessionId(request != null ? request.getSession().getId() : null)
                .ipAddress(request != null ? getClientIp(request) : null)
                .userAgent(request != null ? request.getHeader("User-Agent") : null);
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}