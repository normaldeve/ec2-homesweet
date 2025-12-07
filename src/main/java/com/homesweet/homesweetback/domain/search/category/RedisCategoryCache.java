package com.homesweet.homesweetback.domain.search.category;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author junnukim1007gmail.com
 * @date 25. 12. 7.
 */
@Service
@RequiredArgsConstructor
public class RedisCategoryCache {

    private static final String KEY_PREFIX = "category:children:";

    private final RedisTemplate<String, Object> redisTemplate;

    public List<Long> getChildren(Long categoryId) {
        String key = KEY_PREFIX + categoryId;
        Object cached = redisTemplate.opsForValue().get(key);
        return cached == null ? List.of() : (List<Long>) cached;
    }
}
