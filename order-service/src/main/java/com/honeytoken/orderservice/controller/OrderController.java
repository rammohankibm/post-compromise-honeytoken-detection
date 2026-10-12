package com.honeytoken.orderservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Legitimate order-service REST controller.
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listOrders() {
        log.info("GET /orders");
        return ResponseEntity.ok(List.of(
                Map.of("id", "ORD-001", "userId", 1, "amount", 49.99, "status", "DELIVERED"),
                Map.of("id", "ORD-002", "userId", 2, "amount", 129.00, "status", "PENDING")
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String id) {
        log.info("GET /orders/{}", id);
        return ResponseEntity.ok(
                Map.of("id", id, "userId", 1, "amount", 49.99, "status", "DELIVERED")
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> body) {
        log.info("POST /orders body={}", body);
        body.put("id", "ORD-" + (int)(Math.random() * 9000 + 1000));
        body.put("status", "CREATED");
        return ResponseEntity.status(201).body(body);
    }
}

