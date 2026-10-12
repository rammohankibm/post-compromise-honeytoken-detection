package com.honeytoken.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a honeytoken stored in Redis.
 * Types: API_KEY, JWT_CLAIM, DB_RECORD, INTERNAL_ENDPOINT
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Honeytoken {

    /** Unique token value (the decoy credential/key/path) */
    private String token;

    /** Type of honeytoken */
    private HoneytokenType type;

    /** Human-readable description */
    private String description;

    /** When this token was created */
    private Instant createdAt;

    /** Whether the token has been triggered */
    private boolean triggered;

    public enum HoneytokenType {
        API_KEY,
        JWT_CLAIM,
        DB_RECORD,
        INTERNAL_ENDPOINT
    }
}

