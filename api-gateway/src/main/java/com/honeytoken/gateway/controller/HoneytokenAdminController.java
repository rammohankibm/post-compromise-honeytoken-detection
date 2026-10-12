package com.honeytoken.gateway.controller;

import com.honeytoken.gateway.model.Honeytoken;
import com.honeytoken.gateway.service.HoneytokenLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Internal admin endpoint for registering / deleting honeytokens at runtime.
 * Bind this to a secured management port in production.
 */
@RestController
@RequestMapping("/admin/honeytokens")
@RequiredArgsConstructor
public class HoneytokenAdminController {

    private final HoneytokenLookupService lookupService;

    @PostMapping("/apikey")
    public Mono<ResponseEntity<String>> registerApiKey(
            @RequestParam String key,
            @RequestParam(defaultValue = "Decoy API key") String description) {
        return lookupService.registerApiKey(key, description)
                .map(ok -> ResponseEntity.ok("API-key honeytoken registered: " + key));
    }

    @PostMapping("/endpoint")
    public Mono<ResponseEntity<String>> registerEndpoint(
            @RequestParam String path,
            @RequestParam(defaultValue = "Decoy internal endpoint") String description) {
        return lookupService.registerEndpoint(path, description)
                .map(ok -> ResponseEntity.ok("Endpoint honeytoken registered: " + path));
    }

    @PostMapping("/jwtclaim")
    public Mono<ResponseEntity<String>> registerJwtClaim(
            @RequestParam String claimValue,
            @RequestParam(defaultValue = "Decoy JWT claim") String description) {
        return lookupService.registerJwtClaim(claimValue, description)
                .map(ok -> ResponseEntity.ok("JWT-claim honeytoken registered: " + claimValue));
    }

    @DeleteMapping
    public Mono<ResponseEntity<String>> delete(
            @RequestParam String token,
            @RequestParam Honeytoken.HoneytokenType type) {
        return lookupService.delete(token, type)
                .map(ok -> ok
                        ? ResponseEntity.ok("Honeytoken deleted: " + token)
                        : ResponseEntity.notFound().<String>build());
    }
}

