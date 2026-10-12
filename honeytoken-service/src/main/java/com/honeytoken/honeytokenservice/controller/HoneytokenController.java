package com.honeytoken.honeytokenservice.controller;

import com.honeytoken.honeytokenservice.model.HoneytokenRecord;
import com.honeytoken.honeytokenservice.service.HoneytokenManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for honeytoken lifecycle management and alert log retrieval.
 *
 * Endpoints:
 *   POST   /honeytokens              - Register a new honeytoken
 *   GET    /honeytokens              - List all registered honeytokens
 *   DELETE /honeytokens/{type}/{tok} - Delete a honeytoken
 *   GET    /honeytokens/alerts       - Retrieve alert log
 *   GET    /honeytokens/metrics      - Summary metrics for evaluation
 */
@RestController
@RequestMapping("/honeytokens")
@RequiredArgsConstructor
public class HoneytokenController {

    private final HoneytokenManagementService service;

    @PostMapping
    public ResponseEntity<String> register(
            @RequestParam String type,
            @RequestParam String token,
            @RequestParam(defaultValue = "Decoy token") String description) {
        service.register(type, token, description);
        return ResponseEntity.status(201).body("Registered [" + type + "] " + token);
    }

    @GetMapping
    public ResponseEntity<List<HoneytokenRecord>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @DeleteMapping("/{type}/{token}")
    public ResponseEntity<String> delete(
            @PathVariable String type,
            @PathVariable String token) {
        boolean deleted = service.delete(type, token);
        return deleted
                ? ResponseEntity.ok("Deleted")
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getAlerts() {
        return ResponseEntity.ok(service.getAlertLog());
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        long alertCount    = service.getAlertCount();
        long totalTokens   = service.listAll().size();
        return ResponseEntity.ok(Map.of(
                "totalHoneytokens", totalTokens,
                "totalAlerts",      alertCount,
                "description",      "Detection count since gateway start"
        ));
    }
}

