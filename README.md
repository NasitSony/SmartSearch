# SmartSearch — Fault-Aware Asynchronous Ingestion + Semantic Retrieval

SmartSearch is a production-style backend designed to maintain **correctness under failure**.

It ingests documents via **Kafka-based asynchronous processing**, persists embeddings in **Postgres/pgvector**, and exposes semantic search and RAG APIs and ensures **idempotency, retry safety, and explicit job state tracking**.

---

## Why this is not another RAG demo

- **Crash recovery:** worker failures do not lose data — ingestion resumes safely
- **Idempotency:** duplicate requests and Kafka replays do not create duplicate chunks
- **Explicit lifecycle:** PENDING → PROCESSING → READY / FAILED
- **Retry system:** failed or stuck jobs can be retried or republished safely
- **Backpressure visibility:** `/api/system/pressure` exposes system load in real time
- **Fast-fail safety:** ingestion is rejected if the database is unavailable

---

## Core Capabilities

- Event-driven ingestion pipeline (API → Kafka → Worker → Postgres)
- Idempotent chunk storage preventing duplicate writes
- Stateful job lifecycle tracking (PENDING → PROCESSING → READY / FAILED)
- Semantic retrieval via pgvector similarity search
- RAG-based question answering with grounded citations

  


------------------------------------------------------------------------

## 🧠 SmartSearch Architecture

<p align="center">
  <img src="docs/images/Architecture.png" width="800"/>
</p>

<p align="center">
  <em>End-to-end architecture: API → Kafka → Async Workers → pgvector → Vector Search</em>
</p>



### Architecture Notes

The system uses Kafka to decouple ingestion from processing, enabling
failure recovery, retry handling, and consistent state transitions
without blocking API responsiveness.

## System Guarantees

- At-least-once ingestion with retry safety
- No duplicate chunks under Kafka replay
- Crash-safe processing with no data corruption
- Deterministic job state transitions (PENDING → PROCESSING → READY / FAILED)

## Failure Scenarios (Tested)

- Worker killed mid-processing → message reprocessed safely
- Worker killed after DB write → no duplicate chunks
- Kafka replay → idempotent writes prevent duplication
- DB temporarily unavailable → retries succeed without data loss

------------------------------------------------------------------------

## API

### POST /api/documents
```bash
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{"requestId":"doc-1","text":"..."}'

GET /api/search
curl "http://localhost:8080/api/search?q=mvba&k=3"

GET /api/ask
curl "http://localhost:8080/api/ask?q=What%20is%20MVBA%3F&k=5"

**Quickstart**
docker compose up
./mvnw spring-boot:run


**Key Design Decisions**
- Idempotency: requestId uniqueness prevents duplicate ingestion
- At-least-once processing: Kafka retries + safe replay handling
- State machine: PENDING → PROCESSING → READY / FAILED
- Failure isolation: ingestion decoupled via Kafka

**Roadmap**
- Observability (metrics + dashboards)
- Performance benchmarking
- Load testing under sustained ingestion
