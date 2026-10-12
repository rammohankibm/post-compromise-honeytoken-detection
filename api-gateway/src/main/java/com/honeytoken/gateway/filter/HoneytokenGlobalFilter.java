package com.honeytoken.gateway.filter;

import com.honeytoken.gateway.metrics.DetectionMetricsEvaluator;
import com.honeytoken.gateway.model.AlertEvent;
import com.honeytoken.gateway.model.Honeytoken;
import com.honeytoken.gateway.service.AlertService;
import com.honeytoken.gateway.service.HoneytokenLookupService;
import com.honeytoken.gateway.service.JwtParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Core honeytoken detection filter for Spring Cloud Gateway.
 *
 * Inspection order:
 *   1. Internal/decoy endpoint check  (path-based)
 *   2. API key honeytoken check       (X-API-Key header)
 *   3. JWT claim honeytoken check     (Authorization: Bearer <jwt>)
 *
 * On detection: alert is raised, request is blocked with 403 Forbidden.
 * Legitimate traffic passes through unchanged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HoneytokenGlobalFilter implements GlobalFilter, Ordered {

    /** Claim name used to embed honeytoken values inside JWTs */
    private static final String HONEY_JWT_CLAIM = "hny";

    private final HoneytokenLookupService honeytokenLookupService;
    private final AlertService alertService;
    private final JwtParserService jwtParserService;
    private final DetectionMetricsEvaluator metricsEvaluator;

    @Override
    public int getOrder() {
        // Run before routing (-1 = very early)
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startNanos = System.nanoTime();

        String path      = exchange.getRequest().getPath().value();
        String apiKey    = exchange.getRequest().getHeaders().getFirst("X-API-Key");
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String sourceIp  = resolveSourceIp(exchange);
        String method    = exchange.getRequest().getMethod().name();
        String userAgent = exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT);

        // 1. Check honeytoken endpoint
        Mono<Optional<Honeytoken>> endpointCheck = honeytokenLookupService
                .checkEndpoint(path)
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());

        // 2. Check honeytoken API key
        Mono<Optional<Honeytoken>> apiKeyCheck = (apiKey != null)
                ? honeytokenLookupService.checkApiKey(apiKey).map(Optional::of).defaultIfEmpty(Optional.empty())
                : Mono.just(Optional.empty());

        // 3. Check JWT claim
        Optional<String> jwtClaimValue = jwtParserService.extractClaim(authHeader, HONEY_JWT_CLAIM);
        Mono<Optional<Honeytoken>> jwtCheck = jwtClaimValue.isPresent()
                ? honeytokenLookupService.checkJwtClaim(jwtClaimValue.get()).map(Optional::of).defaultIfEmpty(Optional.empty())
                : Mono.just(Optional.empty());

        return Mono.zip(endpointCheck, apiKeyCheck, jwtCheck)
                .flatMap(tuple -> {
                    Optional<Honeytoken> endpointHoney = tuple.getT1();
                    Optional<Honeytoken> apiKeyHoney   = tuple.getT2();
                    Optional<Honeytoken> jwtHoney      = tuple.getT3();

                    Optional<Honeytoken> triggered = endpointHoney
                            .or(() -> apiKeyHoney)
                            .or(() -> jwtHoney);

                    if (triggered.isPresent()) {
                        Honeytoken ht = triggered.get();
                        long latencyMs = (System.nanoTime() - startNanos) / 1_000_000;

                        AlertEvent alert = AlertEvent.builder()
                                .honeytokenValue(ht.getToken())
                                .honeytokenType(ht.getType())
                                .sourceIp(sourceIp)
                                .httpMethod(method)
                                .requestPath(path)
                                .userAgent(userAgent)
                                .authorizationHeader(redactAuth(authHeader))
                                .detectedAt(Instant.now())
                                .severity("HIGH")
                                .message(String.format(
                                        "Honeytoken accessed [type=%s] [token=%s] [latency=%dms]",
                                        ht.getType(), ht.getToken(), latencyMs))
                                .build();

                        alertService.raiseAlert(alert);
                        metricsEvaluator.recordTruePositive(latencyMs);

                        // Block the request — attackers must not reach real services
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    // Legitimate request — continue routing
                    metricsEvaluator.recordLegitimateRequest();
                    return chain.filter(exchange);
                });
    }

    // ------------------------------------------------------------------ helpers

    private String resolveSourceIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                .getAddress().getHostAddress();
    }

    private String redactAuth(String authHeader) {
        if (authHeader == null) return null;
        if (authHeader.length() > 20) {
            return authHeader.substring(0, 15) + "...[REDACTED]";
        }
        return "[REDACTED]";
    }
}





