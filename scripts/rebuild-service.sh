#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# rebuild-service.sh <service-name>
#
# Rebuilds and restarts a single service without touching the others.
#
# Usage:
#   ./rebuild-service.sh compliance
#   ./rebuild-service.sh competition
#   ./rebuild-service.sh finance
# ─────────────────────────────────────────────────────────────────────────────
set -e

SERVICE="${1:?Usage: ./rebuild-service.sh <service-name>}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "→ Building JAR for ${SERVICE}-service..."
mvn package -pl "services/${SERVICE}-service" -am -DskipTests -q

echo "→ Rebuilding Docker image for ${SERVICE}-service..."
docker-compose build "${SERVICE}-service"

echo "→ Restarting ${SERVICE}-service..."
docker-compose up -d "${SERVICE}-service"

echo "✓ Done. Tailing logs (Ctrl+C to exit):"
docker-compose logs -f "${SERVICE}-service"
