#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# build-and-run.sh
#
# Builds all service JARs locally with Maven, then starts the full stack
# with docker-compose.  Run this instead of plain `docker-compose up`.
#
# Requirements:
#   - Java 25 installed locally  (sdk install java 25-open  if using SDKMAN)
#   - Maven 3.9+
#   - Docker + Docker Compose
# ─────────────────────────────────────────────────────────────────────────────
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo "══════════════════════════════════════════════════════"
echo "  Step 1 — Maven build (all services, skip tests)"
echo "══════════════════════════════════════════════════════"
mvn clean package -DskipTests

echo ""
echo "══════════════════════════════════════════════════════"
echo "  Step 2 — docker-compose build (copy JARs only)"
echo "══════════════════════════════════════════════════════"
docker-compose build

echo ""
echo "══════════════════════════════════════════════════════"
echo "  Step 3 — Start full stack"
echo "══════════════════════════════════════════════════════"
docker-compose up -d

echo ""
echo "✓ Stack is starting. Check logs with:"
echo "    docker-compose logs -f compliance-service"
echo "    docker-compose logs -f competition-service"
echo ""
echo "  Kafka UI → http://localhost:9000"
echo ""
