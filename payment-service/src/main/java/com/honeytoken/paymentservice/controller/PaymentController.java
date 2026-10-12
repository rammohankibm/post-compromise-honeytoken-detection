package com.honeytoken.paymentservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Legitimate payment-service REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> body) {
        log.info("POST /payments/process body={}", body);
        return ResponseEntity.ok(Map.of(
                "transactionId", "TXN-" + (int)(Math.random() * 999999),
                "status", "APPROVED",
                "amount", body.getOrDefault("amount", 0),
                "currency", body.getOrDefault("currency", "USD")
        ));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable String transactionId) {
        log.info("GET /payments/{}", transactionId);
        return ResponseEntity.ok(Map.of(
                "transactionId", transactionId,
                "status", "APPROVED",
                "amount", 99.99
        ));
    }
}

