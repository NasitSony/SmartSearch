# SmartSearch — Fault-Aware Asynchronous Ingestion + Semantic Retrieval

🚀 SmartSearch

Fault-Aware Async Ingestion + Semantic Retrieval Backend

A production-style backend that ingests documents asynchronously, generates embeddings, and serves semantic search and RAG — with explicit guarantees for **idempotency, crash recovery, and job lifecycle correctness**.

---

## 🎯 What this project is

This is **not just another RAG demo**.

It is a **correctness-first ingestion and retrieval system** designed to answer:

### What happens when things fail?
- What if the worker crashes mid-processing?
- What if Kafka replays messages?
- What if the database goes down?
- What if duplicate requests arrive?

This system is built to **handle those scenarios deterministically**.

## 🧠 Core Design Philosophy
- Correctness over convenience
- Explicit state over hidden progress
- Failure-aware design
- Deterministic recovery

---

## 🏗️ Architecture Overview

<p align="center">
  <img src="docs/images/Architecture.png" width="400"/>
</p>

<p align="center">
  <em>End-to-end architecture: API → Kafka → Async Workers → pgvector → Vector Search</em>
</p>


**Components**
- ***API Service***
  - Accepts ingestion requests
  - Produces messages to Kafka
  - Exposes search + RAG endpoints
- ***Kafka***
  - Decouples ingestion from processing
  - Enables retry and replay
- ***Worker***
  - Consumes messages
  - Generates embeddings
  - Writes to Postgres
- ***Postgres + pgvector***
  - Stores embeddings
  - Enables similarity search
 
## 📈 System Evolution (KV-Engine Style)
 🟢 **Stage 0 — Synchronous ingestion (baseline)**
  API directly:
  - generates embeddings
  - stores in DB

 ❌ Problems:
 - slow
 - no retry
 - no failure recovery

🟡 **Stage 1 — Asynchronous ingestion**
- API produces message to Kafka
- Worker processes asynchronously

✔️ Benefits:
- decoupling
- retry support
- resilience

🟠 **Stage 2 — Job lifecycle state machine**

Each request has explicit states:
PENDING -> PROCESSING -> READY | FAILED

✔️ Guarantees:
- no hidden progress
- observable system state
- debuggable failures

🔵 **Stage 3 — Idempotent ingestion**
- Duplicate messages (Kafka replay / retries) are safe
- Enforced via:
  - unique constraints (e.g., docId + chunkId)
  - deterministic writes

✔️ Guarantees:
- no duplicate embeddings
- safe reprocessing

🔴 **Stage 4 — Failure handling + retries**
- Worker retries failed jobs (bounded attempts)
- After retry exhaustion:
  - job marked FAILED
  - message sent to DLQ

✔️ Guarantees:
- no infinite retry loops
- bad data is isolated

🟣 **Stage 5 — Observability**

System exposes:
- /api/system/pressure
  - pending jobs
  - processing jobs
  - failed jobs
  - ready jobs
- logs:
  - job transitions
  - end-to-end latency

✔️ Guarantees:
- system is observable
- failures are visible


## ✅ System Guarantees
**Correctness**
- At-least-once ingestion (via Kafka)
- Idempotent processing (no duplicate chunks)
- Deterministic job lifecycle

**Failure Safety**
- Worker crash does not corrupt state
- Partial processing is recoverable
- Retries are safe

**Data Integrity**
- No duplicate embeddings
- No partial chunk visibility
- Consistent DB state after recovery

## ⚠️ Failure Matrix
| Failure Scenario            | Expected Behavior                                |
| --------------------------- | ------------------------------------------------ |
| Worker crash mid-processing | Job is retried and completes successfully        |
| Worker crash after DB write | Reprocessing occurs but duplicates are prevented |
| Kafka broker restart        | Processing resumes with no data loss             |
| Postgres outage             | Worker retries; job eventually READY or FAILED   |
| Poison message              | Retries exhausted → FAILED + DLQ                 |
| Duplicate request           | No duplicate embeddings created                  |


------------------------------------------------------------------------

## 🧪 Failure Proof (Reproducible Tests)
**T1 — Crash mid-processing**
- Submit document
- Kill worker during processing

✔️ Expected:
- Job resumes
- No duplicate chunks

**T2 — Crash after DB write**
- Kill worker after write but before commit acknowledgment

✔️ Expected:
- Reprocessing occurs
- No duplicates (idempotency holds)

**T3 — Kafka restart**
- Stop Kafka during ingestion
- Restart Kafka

✔️ Expected:
- Worker resumes
- No message loss

**T4 — Database outage**
- Stop Postgres
- Submit job
- Restart DB

✔️ Expected:
- Worker retries
- Job becomes READY or FAILED

**T5 — Poison message**
- Submit malformed document

✔️ Expected:
- Retries attempted
- Job marked FAILED
- Message sent to DLQ

## 🔍 API Endpoints (example)
**Ingestion**
```bash
POST /api/documents
```
**Search**
```bash
GET /api/search?q=...
```

**RAG**
```bash
POST /api/rag
```

**System Pressure**
```bash
GET /api/system/pressure
```

## 🚀 Why this matters
Most AI systems focus on:
- embeddings
- LLMs
- retrieval quality

This project focuses on:
 **What happens when the system breaks.**
That’s what differentiates:
- demos -> production systems
- prototypes -> infrastructure

## 🧭 Future Work
- Exactly-once semantics (Kafka transactions)
- Distributed workers + partition-aware scaling
- Backpressure-aware scheduling
- Streaming ingestion
- BFT-style replicated ingestion pipeline (long-term vision)

## 🔗 Positioning
This project sits at the intersection of:
- Distributed systems
- AI infrastructure
- Fault-tolerant pipelines
It is designed as a **foundation layer for reliable AI systems**.

## 💡 Author Note
This system reflects a broader focus on:
-fault tolerance
- correctness guarantees
- distributed system design

Similar to how storage engines ensure durability and consistency,
this system ensures **reliable AI data pipelines under failure**.

