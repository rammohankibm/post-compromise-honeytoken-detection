package com.honeytoken.honeytokenservice.service;

import com.honeytoken.honeytokenservice.model.HoneytokenRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manages honeytoken lifecycle: creation, retrieval, deletion, and alert tracking.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HoneytokenManagementService {

    private static final String PREFIX_APIKEY   = "honeytoken:apikey:";
    private static final String PREFIX_ENDPOINT = "honeytoken:endpoint:";
    private static final String PREFIX_JWTCLAIM = "honeytoken:jwtclaim:";
    private static final String ALERT_LOG_KEY   = "honeytoken:alerts";

    private final StringRedisTemplate redisTemplate;

    public void register(String type, String token, String description) {
        String key = buildKey(type, token);
        redisTemplate.opsForValue().set(key, description);
        log.info("Registered honeytoken [{} -> {}]", type, token);
    }

    public boolean delete(String type, String token) {
        String key = buildKey(type, token);
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }

    public List<HoneytokenRecord> listAll() {
        List<HoneytokenRecord> records = new ArrayList<>();
        collectByPrefix(PREFIX_APIKEY,   "API_KEY",           records);
        collectByPrefix(PREFIX_ENDPOINT, "INTERNAL_ENDPOINT", records);
        collectByPrefix(PREFIX_JWTCLAIM, "JWT_CLAIM",         records);
        return records;
    }

    /**
     * Record an alert event in Redis (used for metrics/evaluation).
     */
    public void recordAlert(String alertJson) {
        redisTemplate.opsForList().rightPush(ALERT_LOG_KEY, alertJson);
        log.warn("ALERT_RECORDED: {}", alertJson);
    }

    public List<String> getAlertLog() {
        Long size = redisTemplate.opsForList().size(ALERT_LOG_KEY);
        if (size == null || size == 0) return List.of();
        return redisTemplate.opsForList().range(ALERT_LOG_KEY, 0, -1);
    }

    public long getAlertCount() {
        Long size = redisTemplate.opsForList().size(ALERT_LOG_KEY);
        return size == null ? 0 : size;
    }

    // ------------------------------------------------------------------ helpers

    private String buildKey(String type, String token) {
        return switch (type.toUpperCase()) {
            case "API_KEY"           -> PREFIX_APIKEY + token;
            case "INTERNAL_ENDPOINT" -> PREFIX_ENDPOINT + token;
            case "JWT_CLAIM"         -> PREFIX_JWTCLAIM + token;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    private void collectByPrefix(String prefix, String type, List<HoneytokenRecord> out) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys == null) return;
        for (String key : keys) {
            String desc = redisTemplate.opsForValue().get(key);
            out.add(HoneytokenRecord.builder()
                    .token(key.substring(prefix.length()))
                    .type(type)
                    .description(desc)
                    .createdAt(Instant.EPOCH)
                    .build());
        }
    }
}

