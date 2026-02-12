# Real-Time Trading System Monitor

## Project Overview
A simulated fixed-income order matching engine with full production-grade monitoring, instrumentation, and alerting — built to demonstrate application support and system reliability skills relevant to financial services.

## Why This Project Matters for Tradeweb
- Shows you understand **trading systems** and **fixed income** concepts
- Demonstrates **system instrumentation, monitoring, and reporting tools**
- Proves you can maintain **high availability** in **mission-critical** environments
- Uses their exact tech stack: **Java/C++, PostgreSQL, Prometheus, Grafana, Docker**

---

## Architecture

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────┐
│  Order Generator │────▶│  Order Matching  │────▶│ PostgreSQL  │
│  (Python script) │     │  Engine (Java)   │     │ (Trade logs)│
└─────────────────┘     └──────────────────┘     └─────────────┘
                              │
                              │ /metrics
                              ▼
                        ┌──────────────┐     ┌──────────────┐
                        │  Prometheus  │────▶│   Grafana    │
                        │  (scraping)  │     │ (dashboards) │
                        └──────────────┘     └──────────────┘
```

---

## Components

### 1. Order Matching Engine (Java - Spring Boot)
- REST API to submit buy/sell orders for bonds
- Simple price-time priority matching logic
- Exposes `/metrics` endpoint (Prometheus format)
- Health check endpoint `/health`
- Logs all trades to PostgreSQL

### 2. Order Generator (Python)
- Simulates random buy/sell orders hitting the engine
- Configurable rate (orders per second)
- Can simulate spikes, failures, and anomalies

### 3. PostgreSQL Database
- Stores: orders, trades, system events
- Demonstrates relational modeling, indexing, query optimization

### 4. Monitoring Stack
- **Prometheus**: scrapes metrics every 15s
- **Grafana**: pre-built dashboards for:
  - Orders per second (throughput)
  - Match latency (p50, p95, p99)
  - Error rate
  - System availability uptime
  - Database connection pool health
- **Alerting rules**: SLA breach, high error rate, latency spike

### 5. Docker Compose (full stack)
- One command: `docker-compose up`
- All 5 services containerized

---

## Weekend Build Plan

### Day 1 (Saturday) — ~6 hours

**Morning (3 hrs): Core Engine**
- [ ] Set up Spring Boot project with Maven
- [ ] Create Order model (id, side, instrument, price, quantity, timestamp)
- [ ] Create Trade model (id, buy_order_id, sell_order_id, price, quantity, timestamp)
- [ ] Build simple order book with price-time priority matching
- [ ] REST endpoints: POST /orders, GET /orders, GET /trades, GET /health

**Afternoon (3 hrs): Database + Metrics**
- [ ] Set up PostgreSQL schema (orders, trades, system_events tables)
- [ ] Add JPA/Hibernate for persistence
- [ ] Add Micrometer + Prometheus metrics:
  - `orders_received_total` (counter)
  - `orders_matched_total` (counter)
  - `order_match_latency_seconds` (histogram)
  - `order_book_depth` (gauge)
  - `errors_total` (counter)
  - `system_uptime_seconds` (gauge)

### Day 2 (Sunday) — ~6 hours

**Morning (3 hrs): Monitoring + Alerts**
- [ ] Write Prometheus config (scrape targets)
- [ ] Write alerting rules (high latency, error rate, availability)
- [ ] Create Grafana dashboard JSON (4-5 panels)
- [ ] Set up Grafana datasource for Prometheus

**Afternoon (3 hrs): Docker + Generator + Polish**
- [ ] Write Dockerfiles for engine, generator
- [ ] Write docker-compose.yml (all 5 services)
- [ ] Build Python order generator with configurable load
- [ ] Test full stack end-to-end
- [ ] Write README with screenshots
- [ ] Push to GitHub

---

## Key Files Structure

```
trading-system-monitor/
├── README.md
├── docker-compose.yml
├── order-engine/                    # Java Spring Boot
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/trademonitor/
│       ├── TradingEngineApplication.java
│       ├── model/
│       │   ├── Order.java
│       │   └── Trade.java
│       ├── repository/
│       │   ├── OrderRepository.java
│       │   └── TradeRepository.java
│       ├── service/
│       │   └── MatchingEngine.java
│       ├── controller/
│       │   ├── OrderController.java
│       │   └── HealthController.java
│       └── config/
│           └── MetricsConfig.java
├── order-generator/                 # Python
│   ├── Dockerfile
│   ├── requirements.txt
│   └── generator.py
├── monitoring/
│   ├── prometheus/
│   │   ├── prometheus.yml
│   │   └── alert_rules.yml
│   └── grafana/
│       ├── datasource.yml
│       └── dashboard.json
└── db/
    └── init.sql
```

---

## Resume Bullet Points (after building)

**Real-Time Trading System Monitor**
*Java, Spring Boot, PostgreSQL, Prometheus, Grafana, Docker*

• Built a simulated fixed-income order matching engine in **Java (OOP)** with REST APIs, demonstrating
  **object-oriented development** and **relational database** design for trade logging in **PostgreSQL**.

• Developed **system instrumentation, monitoring, and reporting tools** using **Prometheus and Grafana**,
  with custom metrics for throughput, latency, error rates, and SLA-based alerting to ensure **high availability**.

• Containerized the full stack using **Docker Compose**, including the matching engine, order generator,
  database, and monitoring infrastructure — simulating a **mission-critical production environment**.

---

## Talking Points for Interview

1. "I built this to understand how trading systems work end-to-end — from order submission to matching to monitoring."
2. "The alerting rules I set up mirror real production SLAs — if match latency exceeds 100ms p95 or error rate goes above 1%, alerts fire."
3. "I can demonstrate how I'd diagnose a production issue: check Grafana dashboards, identify the spike, trace it through logs, and resolve it."
4. "The order book uses price-time priority, which is the standard matching algorithm in fixed-income markets."
