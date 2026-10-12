package com.honeytoken.gateway.controller;

import com.honeytoken.gateway.metrics.DetectionMetricsEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes detection evaluation metrics at /admin/metrics.
 */
@RestController
@RequestMapping("/admin/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final DetectionMetricsEvaluator evaluator;

    @GetMapping
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(evaluator.getSummary());
    }
}

