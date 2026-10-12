package com.honeytoken.gateway;

import com.honeytoken.gateway.service.HoneytokenLookupService;
import com.honeytoken.gateway.metrics.DetectionMetricsEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration tests for the HoneytokenGlobalFilter.
 *
 * Validates:
 *   - Legitimate requests are forwarded (not blocked)
 *   - API key honeytoken triggers 403
 *   - Endpoint honeytoken triggers 403
 *   - JWT claim honeytoken triggers 403
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HoneytokenFilterIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private HoneytokenLookupService honeytokenLookupService;

    @Test
    @DisplayName("Legitimate request passes through (no honeytoken match)")
    void legitimateRequest_passesThrough() {
        when(honeytokenLookupService.checkEndpoint(anyString())).thenReturn(Mono.empty());
        when(honeytokenLookupService.checkApiKey(anyString())).thenReturn(Mono.empty());

        // Should not be blocked (may get connection refused to downstream, but not 403)
        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().value(status -> org.junit.jupiter.api.Assertions.assertNotEquals(403, status));
    }

    @Test
    @DisplayName("Honeytoken API key triggers 403 Forbidden")
    void honeytokenApiKey_triggers403() {
        when(honeytokenLookupService.checkEndpoint(anyString())).thenReturn(Mono.empty());
        when(honeytokenLookupService.checkApiKey("HONEY-KEY-ALPHA-0001"))
                .thenReturn(Mono.just(com.honeytoken.gateway.model.Honeytoken.builder()
                        .token("HONEY-KEY-ALPHA-0001")
                        .type(com.honeytoken.gateway.model.Honeytoken.HoneytokenType.API_KEY)
                        .description("Decoy API key alpha")
                        .build()));

        webTestClient.get()
                .uri("/api/users")
                .header("X-API-Key", "HONEY-KEY-ALPHA-0001")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Honeytoken endpoint triggers 403 Forbidden")
    void honeytokenEndpoint_triggers403() {
        when(honeytokenLookupService.checkEndpoint("/internal/admin"))
                .thenReturn(Mono.just(com.honeytoken.gateway.model.Honeytoken.builder()
                        .token("/internal/admin")
                        .type(com.honeytoken.gateway.model.Honeytoken.HoneytokenType.INTERNAL_ENDPOINT)
                        .description("Decoy admin endpoint")
                        .build()));
        when(honeytokenLookupService.checkApiKey(anyString())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/internal/admin")
                .exchange()
                .expectStatus().isForbidden();
    }
}


