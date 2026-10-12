package com.honeytoken.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Alert event raised when a honeytoken is accessed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {

    /** The honeytoken value that was accessed */
    private String honeytokenValue;

    /** Type of honeytoken */
    private Honeytoken.HoneytokenType honeytokenType;

    /** Source IP of the attacker */
    private String sourceIp;

    /** HTTP method used */
    private String httpMethod;

    /** Request path */
    private String requestPath;

    /** User-Agent header */
    private String userAgent;

    /** Authorization header (redacted for logging) */
    private String authorizationHeader;

    /** Timestamp of the intrusion attempt */
    private Instant detectedAt;

    /** Severity level */
    private String severity;

    /** Additional context/message */
    private String message;
}

