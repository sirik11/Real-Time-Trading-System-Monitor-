package com.trademonitor.controller;

import com.trademonitor.model.Order;
import com.trademonitor.model.Trade;
import com.trademonitor.service.MatchingEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class OrderController {

    private final MatchingEngine matchingEngine;
    private final long startTime = System.currentTimeMillis();

    public OrderController(MatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> submitOrder(@RequestBody Order order) {
        Order result = matchingEngine.submitOrder(order);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOpenOrders() {
        return ResponseEntity.ok(matchingEngine.getOpenOrders());
    }

    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> getRecentTrades(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(matchingEngine.getRecentTrades(limit));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        long uptimeMs = System.currentTimeMillis() - startTime;
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "uptime_seconds", uptimeMs / 1000,
                "service", "order-matching-engine",
                "version", "1.0.0"
        ));
    }
}
