# Real-Time Trading System Monitor

A simulated fixed-income order matching engine with production-grade monitoring, instrumentation, and alerting — demonstrating application support and system reliability skills for financial services environments.

## Architecture

```
Order Generator (Python) → Order Matching Engine (Java/Spring Boot) → PostgreSQL
                                      ↓ /metrics
                              Prometheus → Grafana (Dashboards + Alerts)
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Matching Engine | Java 17, Spring Boot, JPA/Hibernate |
| Database | PostgreSQL 15 |
| Order Generator | Python 3.11 |
| Metrics | Micrometer + Prometheus |
| Dashboards | Grafana |
| Containerization | Docker, Docker Compose |

## Quick Start

```bash
# Clone the repo
git clone https://github.com/sirik11/trading-system-monitor.git
cd trading-system-monitor

# Start everything
docker-compose up --build

# Access:
# - Order Engine API: http://localhost:8080
# - Prometheus:       http://localhost:9090
# - Grafana:          http://localhost:3000 (admin/admin)
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Submit a buy/sell order |
| GET | `/orders` | List open orders |
| GET | `/trades?limit=50` | Recent trades |
| GET | `/health` | Service health check |
| GET | `/actuator/prometheus` | Prometheus metrics |

## Custom Metrics (System Instrumentation)

| Metric | Type | Description |
|--------|------|-------------|
| `orders_received_total` | Counter | Total orders submitted |
| `orders_matched_total` | Counter | Total successful matches |
| `order_match_latency_seconds` | Histogram | Match latency (p50, p95, p99) |
| `order_book_depth` | Gauge | Current open order count |
| `errors_total` | Counter | Total errors encountered |

## Alert Rules

| Alert | Condition | Severity |
|-------|-----------|----------|
| HighErrorRate | >0.1 errors/sec for 2m | Critical |
| HighMatchLatency | p95 > 100ms for 3m | Warning |
| LowThroughput | <0.5 orders/sec for 5m | Warning |
| ServiceDown | Unreachable for 1m | Critical |
| HighOrderBookDepth | >10,000 open orders for 5m | Warning |

## Instruments Supported

Simulated fixed-income bonds:
- US-2Y-BOND, US-5Y-BOND, US-10Y-BOND, US-30Y-BOND, EU-10Y-BUND

## Sample Order

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"side": "BUY", "instrument": "US-10Y-BOND", "price": 97.25, "quantity": 1000}'
```

## Key Design Decisions

- **Price-time priority matching** — standard algorithm used in fixed-income markets
- **Prometheus metrics** — custom instrumentation for real-time system health visibility
- **SLA-based alerting** — alerts mirror production SLAs (latency, error rate, availability)
- **Containerized** — full stack runs with one `docker-compose up` command
- **Relational database** — PostgreSQL with proper indexing for trade audit trail
