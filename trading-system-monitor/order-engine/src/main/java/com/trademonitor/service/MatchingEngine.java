package com.trademonitor.service;

import com.trademonitor.model.Order;
import com.trademonitor.model.Trade;
import com.trademonitor.repository.OrderRepository;
import com.trademonitor.repository.TradeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MatchingEngine {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;

    // Prometheus metrics
    private final Counter ordersReceived;
    private final Counter ordersMatched;
    private final Counter errorsTotal;
    private final Timer matchLatency;
    private final AtomicInteger orderBookDepth = new AtomicInteger(0);

    public MatchingEngine(OrderRepository orderRepository,
                          TradeRepository tradeRepository,
                          MeterRegistry meterRegistry) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;

        // Register custom metrics for system instrumentation
        this.ordersReceived = Counter.builder("orders_received_total")
                .description("Total orders received")
                .register(meterRegistry);

        this.ordersMatched = Counter.builder("orders_matched_total")
                .description("Total orders matched into trades")
                .register(meterRegistry);

        this.errorsTotal = Counter.builder("errors_total")
                .description("Total errors encountered")
                .register(meterRegistry);

        this.matchLatency = Timer.builder("order_match_latency_seconds")
                .description("Time to match an order")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        Gauge.builder("order_book_depth", orderBookDepth, AtomicInteger::get)
                .description("Current number of open orders")
                .register(meterRegistry);
    }

    @Transactional
    public Order submitOrder(Order order) {
        ordersReceived.increment();

        try {
            // Save the incoming order
            Order savedOrder = orderRepository.save(order);
            orderBookDepth.incrementAndGet();

            // Try to match
            matchLatency.record(() -> tryMatch(savedOrder));

            return savedOrder;
        } catch (Exception e) {
            errorsTotal.increment();
            throw e;
        }
    }

    private void tryMatch(Order incomingOrder) {
        String oppositeSide = incomingOrder.getSide().equals("BUY") ? "SELL" : "BUY";

        // Find matching orders: opposite side, same instrument, compatible price
        List<Order> candidates = orderRepository.findMatchCandidates(
                oppositeSide,
                incomingOrder.getInstrument(),
                incomingOrder.getPrice(),
                incomingOrder.getSide().equals("BUY")
        );

        int remainingQty = incomingOrder.getQuantity();

        for (Order candidate : candidates) {
            if (remainingQty <= 0) break;

            int matchQty = Math.min(remainingQty, candidate.getQuantity());

            // Determine trade price (price-time priority: use resting order's price)
            BigDecimal tradePrice = candidate.getPrice();

            // Create trade
            Long buyId = incomingOrder.getSide().equals("BUY") ? incomingOrder.getId() : candidate.getId();
            Long sellId = incomingOrder.getSide().equals("SELL") ? incomingOrder.getId() : candidate.getId();

            Trade trade = new Trade(buyId, sellId, incomingOrder.getInstrument(), tradePrice, matchQty);
            tradeRepository.save(trade);
            ordersMatched.increment();

            // Update quantities
            remainingQty -= matchQty;
            int candidateRemaining = candidate.getQuantity() - matchQty;

            if (candidateRemaining == 0) {
                candidate.setStatus("FILLED");
                orderBookDepth.decrementAndGet();
            } else {
                candidate.setQuantity(candidateRemaining);
                candidate.setStatus("PARTIAL");
            }
            orderRepository.save(candidate);
        }

        // Update incoming order status
        if (remainingQty == 0) {
            incomingOrder.setStatus("FILLED");
            orderBookDepth.decrementAndGet();
        } else if (remainingQty < incomingOrder.getQuantity()) {
            incomingOrder.setQuantity(remainingQty);
            incomingOrder.setStatus("PARTIAL");
        }
        orderRepository.save(incomingOrder);
    }

    public List<Order> getOpenOrders() {
        return orderRepository.findByStatus("OPEN");
    }

    public List<Trade> getRecentTrades(int limit) {
        return tradeRepository.findTopNByOrderByExecutedAtDesc(limit);
    }
}
