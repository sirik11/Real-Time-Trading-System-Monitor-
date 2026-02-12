"""
Order Generator - Simulates trading activity against the matching engine.
Sends random buy/sell orders for fixed-income instruments.
"""

import os
import time
import random
import requests
import logging
from datetime import datetime

logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)s] %(message)s')
logger = logging.getLogger(__name__)

ENGINE_URL = os.getenv("ENGINE_URL", "http://localhost:8080")
ORDERS_PER_SECOND = int(os.getenv("ORDERS_PER_SECOND", "5"))

# Simulated fixed-income instruments
INSTRUMENTS = [
    "US-2Y-BOND",
    "US-5Y-BOND",
    "US-10Y-BOND",
    "US-30Y-BOND",
    "EU-10Y-BUND",
]

# Base prices for each instrument
BASE_PRICES = {
    "US-2Y-BOND": 99.50,
    "US-5Y-BOND": 98.75,
    "US-10Y-BOND": 97.25,
    "US-30Y-BOND": 94.50,
    "EU-10Y-BUND": 96.80,
}


def generate_order():
    """Generate a random order with realistic fixed-income pricing."""
    instrument = random.choice(INSTRUMENTS)
    side = random.choice(["BUY", "SELL"])
    base_price = BASE_PRICES[instrument]

    # Add small random spread (simulating bid/ask)
    spread = random.uniform(-0.25, 0.25)
    if side == "BUY":
        price = round(base_price - abs(spread), 4)
    else:
        price = round(base_price + abs(spread), 4)

    quantity = random.choice([100, 200, 500, 1000, 2000, 5000])

    return {
        "side": side,
        "instrument": instrument,
        "price": price,
        "quantity": quantity,
    }


def send_order(order):
    """Submit an order to the matching engine."""
    try:
        response = requests.post(f"{ENGINE_URL}/orders", json=order, timeout=5)
        if response.status_code == 200:
            result = response.json()
            logger.info(
                f"{order['side']} {order['quantity']} {order['instrument']} "
                f"@ {order['price']} -> status: {result.get('status', 'unknown')}"
            )
        else:
            logger.warning(f"Order rejected: {response.status_code} - {response.text}")
    except requests.exceptions.ConnectionError:
        logger.error("Cannot connect to order engine - retrying...")
    except requests.exceptions.Timeout:
        logger.error("Order engine timeout")


def wait_for_engine():
    """Wait for the order engine to be healthy before starting."""
    logger.info(f"Waiting for order engine at {ENGINE_URL}...")
    for attempt in range(60):
        try:
            resp = requests.get(f"{ENGINE_URL}/health", timeout=3)
            if resp.status_code == 200:
                logger.info("Order engine is healthy! Starting order generation.")
                return True
        except:
            pass
        time.sleep(2)
    logger.error("Order engine not available after 120 seconds.")
    return False


def main():
    if not wait_for_engine():
        return

    logger.info(f"Generating {ORDERS_PER_SECOND} orders/second")
    delay = 1.0 / ORDERS_PER_SECOND

    order_count = 0
    while True:
        order = generate_order()
        send_order(order)
        order_count += 1

        # Every 100 orders, log a summary
        if order_count % 100 == 0:
            logger.info(f"--- {order_count} orders sent so far ---")

        # Add slight jitter to make it more realistic
        jitter = random.uniform(0.8, 1.2)
        time.sleep(delay * jitter)


if __name__ == "__main__":
    main()
