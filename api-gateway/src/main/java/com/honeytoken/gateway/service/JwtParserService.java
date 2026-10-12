package com.honeytoken.gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Utility service for parsing JWT tokens from the Authorization header.
 * Extracts claims for honeytoken detection without enforcing signature
 * (we want to detect even forged/replayed tokens).
 */
@Slf4j
@Service
public class JwtParserService {

    /**
     * Extracts a claim value by name from a Bearer JWT token.
     * Returns empty if header is absent or parsing fails.
     *
     * @param authorizationHeader The full "Bearer <token>" header value
     * @param claimName           The JWT claim to extract
     */
    public Optional<String> extractClaim(String authorizationHeader, String claimName) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }
        String token = authorizationHeader.substring(7);
        try {
            // Parse without signature verification to catch token replay with any key
            int i = token.lastIndexOf('.');
            String unsignedToken = token.substring(0, i + 1);
            Claims claims = Jwts.parser()
                    .unsecured()
                    .build()
                    .parseUnsecuredClaims(unsignedToken)
                    .getPayload();
            Object value = claims.get(claimName);
            return value == null ? Optional.empty() : Optional.of(value.toString());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT parse failed (may be signed): {}", e.getMessage());
            // Fallback: try to base64-decode the payload for claim extraction
            try {
                String[] parts = token.split("\\.");
                if (parts.length >= 2) {
                    String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                    com.fasterxml.jackson.databind.ObjectMapper mapper =
                            new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(payloadJson);
                    if (node.has(claimName)) {
                        return Optional.of(node.get(claimName).asText());
                    }
                }
            } catch (Exception ex) {
                log.debug("JWT payload decode failed: {}", ex.getMessage());
            }
            return Optional.empty();
        }
    }
}

