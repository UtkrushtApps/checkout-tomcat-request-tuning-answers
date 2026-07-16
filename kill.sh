#!/usr/bin/env bash
set -e

echo "==> Stopping and removing docker compose services..."
docker compose -f /root/task/docker-compose.yml down --volumes --remove-orphans || true

echo "==> Pruning dangling docker resources..."
docker system prune -a --volumes -f || true

echo "==> Removing task-related docker images..."
docker rmi -f $(docker images -q 2>/dev/null | head -20) || true

echo "==> Removing build artifacts and runtime scratch..."
rm -rf /root/task/target || true
rm -rf /root/task/logs /root/task/work /root/task/temp || true
rm -rf /root/task/pgdata /root/task/postgres-data || true

echo "==> Removing task directory..."
rm -rf /root/task || true

echo "Cleanup completed successfully! Droplet is now clean."
