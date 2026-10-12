package com.honeytoken.userservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Legitimate user-service REST controller.
 * Exposes standard CRUD endpoints for users.
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listUsers() {
        log.info("GET /users");
        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "username", "alice", "email", "alice@example.com"),
                Map.of("id", 2, "username", "bob",   "email", "bob@example.com")
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable int id) {
        log.info("GET /users/{}", id);
        return ResponseEntity.ok(
                Map.of("id", id, "username", "user_" + id, "email", "user" + id + "@example.com")
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> body) {
        log.info("POST /users body={}", body);
        body.put("id", (int)(Math.random() * 1000));
        return ResponseEntity.status(201).body(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        log.info("DELETE /users/{}", id);
        return ResponseEntity.noContent().build();
    }
}

