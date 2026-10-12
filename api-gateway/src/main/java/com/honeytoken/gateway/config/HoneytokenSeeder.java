package com.honeytoken.gateway.config;

import com.honeytoken.gateway.service.HoneytokenLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds default honeytokens into Redis when the gateway starts.
 * These decoy tokens serve no legitimate function; any access to them
 * immediately signals malicious activity.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HoneytokenSeeder {

    private final HoneytokenLookupService lookupService;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        log.info("Seeding default honeytokens into Redis...");

        // Decoy API keys
        lookupService.registerApiKey("HONEY-KEY-ALPHA-0001", "Decoy API key alpha").subscribe();
        lookupService.registerApiKey("HONEY-KEY-BETA-9999",  "Decoy API key beta").subscribe();

        // Decoy internal endpoints (common recon targets)
        lookupService.registerEndpoint("/internal/admin",        "Decoy admin endpoint").subscribe();
        lookupService.registerEndpoint("/internal/config",       "Decoy config endpoint").subscribe();
        lookupService.registerEndpoint("/internal/debug",        "Decoy debug endpoint").subscribe();
        lookupService.registerEndpoint("/actuator/env",          "Decoy actuator env endpoint").subscribe();
        lookupService.registerEndpoint("/api/v1/internal/users", "Decoy internal users endpoint").subscribe();

        // Decoy JWT claim values
        lookupService.registerJwtClaim("honey-role-admin-9x",  "Decoy JWT admin role claim").subscribe();
        lookupService.registerJwtClaim("honey-svc-secret-007", "Decoy JWT service secret claim").subscribe();

        log.info("Honeytoken seeding complete.");
    }
}

