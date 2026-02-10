# AI‑Powered Recommendation Engine for Documents (SmartSearch)

> 🚧 This project is being **built in public** to explore how AI-powered search systems evolve when real backend and distributed-systems concerns are introduced.

SmartSearch is an AI-powered document search and recommendation backend, starting with semantic search and RAG and evolving toward reliable, production-style distributed services, built with:

-   Java + Spring Boot
-   Spring AI (EmbeddingModel + ChatModel)
-   PostgreSQL + pgvector
-   Chunk‑level semantic search with RAG responses

------------------------------------------------------------------------

## 🔎 What It Does

This project provides APIs to:

### 📥 Ingest documents

Breaks large text documents into chunks and embeds each chunk.

### 📚 Semantic search

Retrieve most relevant chunks for a query using vector similarity.

### 🤖 RAG Q&A

Answer questions using the retrieved chunks as evidence, with citations.

------------------------------------------------------------------------

## 🚀 Features

### 🧠 Chunking + Embeddings

-   Paragraph‑level document chunking
-   Embeds each chunk using an embedding model

### 📍 pgvector Semantic Search

-   Stores chunk vectors in PostgreSQL with pgvector
-   Fast similarity search using `<->` distance operator

### 🗣️ RAG API

-   `/api/ask`: Answers questions grounded in retrieved chunks with
    citations like `[docId#chunkId]`


### 🧵 Asynchronous Ingestion with Kafka (v0.6)

To decouple API responsiveness from embedding and storage workflows, SmartSearch introduces **Kafka-based asynchronous ingestion**.

- API layer publishes ingestion requests as events
- Worker service consumes events and performs:
  - document chunking
  - embedding generation
  - vector persistence
- Request lifecycle is tracked explicitly (`PENDING → SUCCESS`)
- Failure scenarios are surfaced rather than hidden (FAILED handling in progress)

This design shifts the system from a synchronous demo pipeline to an **event-driven backend**, exposing real-world reliability and correctness challenges.    

------------------------------------------------------------------------

## 🧱 Architecture

Client \| \| POST /api/documents \| GET /api/search?q=...&k=... \| GET
/api/ask?q=...&k=... v Spring MVC Controller v Service Layer -
DocumentService: chunk → embed → store - RagService: retrieve → prompt →
LLM generate v JdbcTemplate + JPA + pgvector v PostgreSQL


Client  
↓  
Spring Boot API  
- POST /api/documents  
- Publishes ingestion event (Kafka)  
↓  
Kafka Topic (`document.ingestion`)  
↓  
Worker / Consumer Service  
- Chunk document  
- Generate embeddings  
- Persist vectors  
↓  
PostgreSQL + pgvector  

Search & RAG Flow (Synchronous):  
Client → API → pgvector similarity search → LLM → Response


### Architecture Notes

Recent iterations introduce asynchronous ingestion to decouple API responsiveness from embedding and storage workflows. This surfaced important reliability concerns around partial failures, retries, and state persistence, which are being addressed incrementally.


------------------------------------------------------------------------

## 📦 API Endpoints

### POST /api/documents

Add or update a document (with chunking and embedding):

``` bash
curl -X POST http://localhost:8080/api/documents   -H "Content-Type: application/json"   -d '{"id":"doc-1","text":"..."}'
```

### GET /api/search

Semantic search over document chunks:

``` bash
curl "http://localhost:8080/api/search?q=mvba&k=3"
```

### GET /api/ask

Retrieval‑Augmented Generation (RAG) question answering:

``` bash
curl "http://localhost:8080/api/ask?q=What%20is%20MVBA%3F&k=5"
```

------------------------------------------------------------------------

## 🚀 Setup (Local Development)

This project consists of an API service, a Kafka-based ingestion pipeline,
and a PostgreSQL database with vector support.

### 1️⃣ Prerequisites
- Java 17+
- PostgreSQL with `vector` extension
- Kafka (recommended via Docker Compose)

### 2️⃣ PostgreSQL
```sql
CREATE DATABASE smartsearch;
\c smartsearch
CREATE EXTENSION IF NOT EXISTS vector;


🔧 Configuration (Kafka + Spring AI)
1️⃣ Kafka (Docker Compose)

Kafka is required for asynchronous ingestion, retries, and DLQ handling.

Create a docker-compose.yml (or use the existing one in the repo):

version: "3.8"

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
Start Kafka locally:

docker compose up kafka zookeeper


Kafka topics are auto-created on first use by the application.

```
2️⃣ Spring AI Configuration

The project uses Spring AI for embeddings and LLM-powered retrieval.

Set your provider API key via environment variables or application.yml.

Example: OpenAI
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
Export your API key:

export OPENAI_API_KEY=your_api_key_here


⚠️ Never commit API keys to source control.

3️⃣ Start the Application

With PostgreSQL, Kafka, and environment variables configured:

./mvnw spring-boot:run


Once running:

API is available at http://localhost:8080

Documents are ingested asynchronously via Kafka

Workers handle retries, idempotency, and DLQ routing


---

### 📌 2) Reliability & Correctness Guarantees

Add this after Features or Architecture:

```md
## 🛡️ Reliability & Correctness Guarantees (v0.7)

This version focuses on validating the system under real-world conditions,
including failures, crashes, and repeated requests.

### 1️⃣ End-to-End Acceptance Testing
- Single and bulk (10+) document ingestion
- Large document handling without timeouts
- Post-ingestion retrieval accuracy

**Result:** The full path from API → Kafka → Worker → Vector Search works reliably.

### 2️⃣ Failure & Crash Resilience (Chaos Tests)
- Worker killed mid-processing → reprocessed safely
- Worker killed after DB write, before commit → no duplicate chunks
- DB unavailable briefly → Kafka retries with no data loss

**Result:** The ingestion pipeline tolerates transient infrastructure failures.

### 3️⃣ Retry, Idempotency & Duplicate Handling
- Consumer exceptions trigger bounded retries
- Exceeded retries are sent to Dead Letter Queue (DLQ)
- Duplicate API requests and Kafka message replays handled safely
  with no duplicate database writes

**Result:** Strong correctness guarantees under retries, failures, and duplicates.

------------------------------------------------------------------------


------------------------------------------------------------------------

## 📝 Example Response

``` json
{
  "question": "What is MVBA?",
  "answer": "MVBA handles multiple values [doc-test#1].",
  "sources": [
    {"docId":"doc-test","chunkId":1,"chunkText":"MVBA handles multiple values.","distance":0.78},
    {"docId":"doc-test","chunkId":0,"chunkText":"Byzantine agreement ensures safety.","distance":1.30}
  ]
}
```

### 4️⃣ Kafka (for async ingestion)

Kafka is used for asynchronous document ingestion in v0.6.

You can run Kafka locally using Docker Compose or any local Kafka setup.
The API publishes ingestion events, and a worker service consumes and processes them.

(Failure handling, retries, and dead-letter queues are being added incrementally.)

------------------------------------------------------------------------


## Project Evolution

SmartSearch is an incremental backend project that started with semantic search and RAG, and is gradually evolving toward production-style reliability and correctness.

- **v0.5 — Semantic Search & RAG Core**
  - Spring AI–based embeddings and chat models
  - PostgreSQL + pgvector for vector similarity search
  - Paragraph-level chunking and retrieval
  - RAG question answering with grounded citations

- **v0.6 — Async Ingestion & Reliability Foundations**
  - Kafka-based event-driven ingestion, decoupling API latency from embedding and persistence
  - Decoupled API and worker-style processing
  - Explicit request lifecycle states (PENDING → SUCCESS)
  - Focus on observability and failure-mode awareness

- **Next — Failure Handling & Correctness**
  - Reliable FAILED-state persistence
  - Retry semantics and idempotent writes
  - Dead-letter handling and error classification
 
## v0.7 — Reliability, Idempotency & Failure Testing

This release focuses on validating the system under real-world failure scenarios,
beyond happy-path execution.

### 1️⃣ End-to-End Acceptance Testing
- Single and bulk (10+) document ingestion verified
- Large document ingestion without timeouts
- Correct retrieval behavior after ingestion

**Result:** The full path from API → Kafka → Worker → Vector Search is stable and correct.

---

### 2️⃣ Failure & Crash Resilience (Chaos Testing)
- Worker killed during processing → message safely reprocessed
- Worker killed after DB write but before commit → no duplicate chunks
- Database unavailable for ~10 seconds → Kafka retries with zero data loss

**Result:** The system tolerates transient infrastructure failures without corruption.

---

### 3️⃣ Retry, Idempotency & Duplicate Handling
- Simulated consumer exceptions trigger bounded retries
- Retry exhaustion routes messages to Dead Letter Queue (DLQ)
- Duplicate API requests and Kafka message replays are handled safely
  with no duplicate database writes

**Result:** Strong correctness guarantees under retries, crashes, and duplicate inputs.
  - 
 
------------------------------------------------------------------------

## 🎯 Motivation

This project demonstrates how to integrate LLMs into traditional Java
backends by:

-   orchestrating data flow between databases and AI models
-   implementing production‑style semantic retrieval pipelines
-   enabling grounded question answering using RAG

It is intended as a foundation for further work on intelligent document
systems and protocol research tools.

------------------------------------------------------------------------


## 💡 Key Design Decisions

**API Idempotency**
- Each request includes a `requestId`
- Database enforces uniqueness
- Duplicate requests return the same document

**Kafka & Exactly-Once Semantics**
- Kafka offsets are committed only after the DB transaction
- Retries are bounded via DefaultErrorHandler
- Permanent failures land in a DLQ


------------------------------------------------------------------------
## Roadmap

- [x] Semantic search with pgvector
- [x] RAG question answering with citations
- [x] Asynchronous ingestion foundation (Kafka-based)
- [x] API idempotency and duplicate request handling
- [x] Retry semantics with bounded attempts
- [x] Dead-letter queue (DLQ) handling for permanent failures
- [x] Crash-safe processing and replay safety

- [ ] Observability: metrics, structured logs, and tracing
- [ ] Operational dashboards and alerting
- [ ] Performance tuning under sustained load

