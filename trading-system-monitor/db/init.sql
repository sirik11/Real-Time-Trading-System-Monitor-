-- Trading System Database Schema

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    side VARCHAR(4) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    instrument VARCHAR(20) NOT NULL,
    price DECIMAL(12, 4) NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(10) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'FILLED', 'PARTIAL', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE trades (
    id SERIAL PRIMARY KEY,
    buy_order_id INTEGER REFERENCES orders(id),
    sell_order_id INTEGER REFERENCES orders(id),
    instrument VARCHAR(20) NOT NULL,
    price DECIMAL(12, 4) NOT NULL,
    quantity INTEGER NOT NULL,
    executed_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE system_events (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    message TEXT,
    severity VARCHAR(10) DEFAULT 'INFO' CHECK (severity IN ('INFO', 'WARN', 'ERROR', 'CRITICAL')),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for query optimization
CREATE INDEX idx_orders_instrument_status ON orders(instrument, status);
CREATE INDEX idx_orders_side_price ON orders(side, price);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_trades_instrument ON trades(instrument);
CREATE INDEX idx_trades_executed_at ON trades(executed_at);
CREATE INDEX idx_system_events_type ON system_events(event_type, created_at);

-- Insert startup event
INSERT INTO system_events (event_type, message, severity)
VALUES ('SYSTEM_START', 'Trading system initialized', 'INFO');
