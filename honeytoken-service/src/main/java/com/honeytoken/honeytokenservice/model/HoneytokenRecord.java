package com.honeytoken.honeytokenservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoneytokenRecord {
    private String token;
    private String type;        // API_KEY | JWT_CLAIM | DB_RECORD | INTERNAL_ENDPOINT
    private String description;
    private Instant createdAt;
    private long triggerCount;
    private Instant lastTriggeredAt;
}

