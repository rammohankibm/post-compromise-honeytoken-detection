package com.honeytoken.gateway.service;

import com.honeytoken.gateway.model.AlertEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for raising and recording intrusion alerts.
 * Logs structured JSON alerts and increments Micrometer counters
 * for monitoring and evaluation (detection rate, false positive rate).
 */
@Slf4j
@Service
public class AlertService {

    private final Counter honeytokenTriggerCounter;
    private final ObjectMapper objectMapper;

    public AlertService(MeterRegistry meterRegistry) {
        this.honeytokenTriggerCounter = Counter.builder("honeytoken.trigger.total")
                .description("Total number of honeytoken trigger events")
                .register(meterRegistry);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Raise an alert for a detected honeytoken access.
     * Writes a structured JSON log entry and increments metrics.
     */
    public void raiseAlert(AlertEvent event) {
        honeytokenTriggerCounter.increment();

        try {
            String json = objectMapper.writeValueAsString(event);
            log.warn("HONEYTOKEN_ALERT: {}", json);
        } catch (Exception e) {
            log.warn("HONEYTOKEN_ALERT: type={} token={} ip={} path={} severity={}",
                    event.getHoneytokenType(),
                    event.getHoneytokenValue(),
                    event.getSourceIp(),
                    event.getRequestPath(),
                    event.getSeverity());
        }
    }
}

