#!/usr/bin/env bash
set -euo pipefail

API="http://localhost:8080"
REQ_ID="chaos-$(date +%s)"

echo "[1] Submit document"
RESP=$(curl -s -X POST "$API/api/documents" \
  -H "Content-Type: application/json" \
  -d "{\"requestId\":\"$REQ_ID\",\"text\":\"Chaos test document about MVBA, retries, and idempotency.\"}")
echo "$RESP"

DOC_ID=$(echo "$RESP" | sed -n 's/.*"docId":"\([^"]*\)".*/\1/p')
if [ -z "${DOC_ID:-}" ]; then
  echo "ERROR: Could not parse docId from response."
  exit 1
fi
echo "[info] docId=$DOC_ID requestId=$REQ_ID"

echo "[2] Kill Spring Boot (simulating worker crash)"
PID=$(lsof -ti :8080 || true)
if [ -z "${PID:-}" ]; then
  echo "ERROR: Nothing is listening on port 8080. Start the app first (./mvnw spring-boot:run)."
  exit 1
fi
kill -9 $PID
echo "[info] killed PID=$PID"

echo "[3] Restart Spring Boot in background"
nohup ./mvnw spring-boot:run > /tmp/smartsearch.log 2>&1 &
sleep 5
echo "[info] restarted. logs: tail -f /tmp/smartsearch.log"

echo "[4] Poll status"
for i in {1..30}; do
  S=$(curl -s "$API/api/documents/$DOC_ID" || true)
  echo "[$i] $S"
  echo "$S" | grep -E '"status":"(READY|FAILED)"' >/dev/null && break
  sleep 2
done

echo "[5] Pressure snapshot"
curl -s "$API/api/system/pressure" || true
echo