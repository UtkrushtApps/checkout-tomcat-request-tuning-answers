#!/usr/bin/env bash
# Simple concurrency probe for exploring CheckoutWeb request behavior.
# Fires N concurrent checkout submissions and prints per-request status and timing.
# Usage: bash scripts/load_test.sh [concurrency]
set -u

BASE_URL="http://127.0.0.1:8080/checkoutweb"
CONCURRENCY="${1:-40}"

echo "Firing ${CONCURRENCY} concurrent POST /checkout/place requests..."

request() {
  local idx="$1"
  local start end code
  start=$(date +%s%3N)
  code=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST "${BASE_URL}/checkout/place" \
        --data "customerId=1&productId=1" \
        --max-time 60)
  end=$(date +%s%3N)
  echo "req=${idx} status=${code} elapsed_ms=$((end - start))"
}

for i in $(seq 1 "${CONCURRENCY}"); do
  request "$i" &
done
wait

echo "Done. Review access logs and thread activity to interpret results."
