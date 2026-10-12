package com.honeytoken.gateway.service;

import com.honeytoken.gateway.model.Honeytoken;
import com.honeytoken.gateway.model.Honeytoken.HoneytokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Service responsible for looking up honeytokens in Redis.
 *
 * Redis key structure:
 *   honeytoken:apikey:<value>         -> type:description
 *   honeytoken:endpoint:<path>        -> type:description
 *   honeytoken:jwtclaim:<claimValue>  -> type:description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HoneytokenLookupService {

    private static final String PREFIX_APIKEY    = "honeytoken:apikey:";
    private static final String PREFIX_ENDPOINT  = "honeytoken:endpoint:";
    private static final String PREFIX_JWTCLAIM  = "honeytoken:jwtclaim:";

    private final ReactiveStringRedisTemplate redisTemplate;

    /**
     * Check whether the given API key is a honeytoken.
     * Returns the Honeytoken if matched, empty otherwise.
     */
    public Mono<Honeytoken> checkApiKey(String apiKey) {
        return redisTemplate.opsForValue()
                .get(PREFIX_APIKEY + apiKey)
                .map(description -> Honeytoken.builder()
                        .token(apiKey)
                        .type(HoneytokenType.API_KEY)
                        .description(description)
                        .build());
    }

    /**
     * Check whether the given request path matches a honeytoken endpoint.
     */
    public Mono<Honeytoken> checkEndpoint(String path) {
        return redisTemplate.opsForValue()
                .get(PREFIX_ENDPOINT + path)
                .map(description -> Honeytoken.builder()
                        .token(path)
                        .type(HoneytokenType.INTERNAL_ENDPOINT)
                        .description(description)
                        .build());
    }

    /**
     * Check whether a JWT claim value is a honeytoken.
     */
    public Mono<Honeytoken> checkJwtClaim(String claimValue) {
        return redisTemplate.opsForValue()
                .get(PREFIX_JWTCLAIM + claimValue)
                .map(description -> Honeytoken.builder()
                        .token(claimValue)
                        .type(HoneytokenType.JWT_CLAIM)
                        .description(description)
                        .build());
    }

    /**
     * Register a honeytoken API key in Redis.
     */
    public Mono<Boolean> registerApiKey(String apiKey, String description) {
        log.info("Registering honeytoken API key: {}", apiKey);
        return redisTemplate.opsForValue()
                .set(PREFIX_APIKEY + apiKey, description);
    }

    /**
     * Register a honeytoken endpoint in Redis.
     */
    public Mono<Boolean> registerEndpoint(String path, String description) {
        log.info("Registering honeytoken endpoint: {}", path);
        return redisTemplate.opsForValue()
                .set(PREFIX_ENDPOINT + path, description);
    }

    /**
     * Register a honeytoken JWT claim in Redis.
     */
    public Mono<Boolean> registerJwtClaim(String claimValue, String description) {
        log.info("Registering honeytoken JWT claim: {}", claimValue);
        return redisTemplate.opsForValue()
                .set(PREFIX_JWTCLAIM + claimValue, description);
    }

    /**
     * Delete a honeytoken by key and type.
     */
    public Mono<Boolean> delete(String token, HoneytokenType type) {
        String key = switch (type) {
            case API_KEY           -> PREFIX_APIKEY + token;
            case INTERNAL_ENDPOINT -> PREFIX_ENDPOINT + token;
            case JWT_CLAIM         -> PREFIX_JWTCLAIM + token;
            default                -> null;
        };
        if (key == null) return Mono.just(false);
        return redisTemplate.delete(key).map(count -> count > 0);
    }
}

