#!/usr/bin/env bash
set -euo pipefail

TASK_DIR="/root/task"
cd "$TASK_DIR"

echo "==> [1/5] Resolving Maven dependencies offline..."
mvn -q -f "$TASK_DIR/pom.xml" dependency:go-offline

echo "==> [2/5] Building WAR artifact and staging Tomcat lib dependencies..."
mvn -q -f "$TASK_DIR/pom.xml" -DskipTests package

if [ ! -f "$TASK_DIR/target/checkoutweb.war" ]; then
  echo "ERROR: WAR build did not produce target/checkoutweb.war" >&2
  exit 1
fi

if [ ! -f "$TASK_DIR/target/tomcat-lib/postgresql-42.6.0.jar" ]; then
  echo "ERROR: PostgreSQL driver was not staged to target/tomcat-lib/" >&2
  exit 1
fi

echo "==> [3/5] Starting services with docker compose..."
docker compose -f "$TASK_DIR/docker-compose.yml" up -d --build

echo "==> [4/5] Waiting for PostgreSQL and Tomcat readiness..."
ATTEMPTS=0
MAX_ATTEMPTS=72
until curl -fsS "http://127.0.0.1:8080/checkoutweb/health" >/dev/null 2>&1; do
  ATTEMPTS=$((ATTEMPTS+1))
  if [ "$ATTEMPTS" -ge "$MAX_ATTEMPTS" ]; then
    echo "ERROR: Tomcat/application did not become ready in time." >&2
    echo "----- recent tomcat logs -----" >&2
    docker compose -f "$TASK_DIR/docker-compose.yml" logs --tail=60 tomcat >&2 || true
    exit 1
  fi
  sleep 5
done

echo "==> [5/5] Smoke-validating database-backed request path..."
ORDERS_HTTP=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:8080/checkoutweb/orders" || true)
if [ "$ORDERS_HTTP" = "200" ]; then
  echo "    Order lookup path responded HTTP 200."
else
  echo "WARNING: /orders returned HTTP ${ORDERS_HTTP}; inspect container logs." >&2
fi

echo ""
echo "========================================================="
echo " CheckoutWeb starter is deployed and ready to explore."
echo " App base:    http://127.0.0.1:8080/checkoutweb/"
echo " Health:      http://127.0.0.1:8080/checkoutweb/health"
echo " Orders:      http://127.0.0.1:8080/checkoutweb/orders"
echo " Checkout:    POST http://127.0.0.1:8080/checkoutweb/checkout/place"
echo ""
echo " Useful logs (inside tomcat container at /usr/local/tomcat/logs):"
echo "   catalina.out  localhost_access_log.*.txt"
echo " Stream logs:  docker compose -f /root/task/docker-compose.yml logs -f tomcat"
echo "========================================================="
