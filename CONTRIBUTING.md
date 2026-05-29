# Contributing to SmartSearch

Thanks for your interest in contributing! SmartSearch is a correctness-first ingestion and retrieval system — contributions that improve reliability, observability, or failure handling are especially welcome.

## Getting Started

### Prerequisites
- Java 21+
- Docker + Docker Compose
- Maven

### Local Setup
```bash
git clone https://github.com/NasitSony/SmartSearch.git
cd SmartSearch
docker compose up -d   # starts Kafka, Postgres, Prometheus, Grafana
mvn clean compile
mvn spring-boot:run
```

Access:
- API: http://localhost:8080
- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090

## How to Contribute

### Reporting Bugs
Open an issue at [GitHub Issues](https://github.com/NasitSony/SmartSearch/issues) and include:
- What you expected to happen
- What actually happened
- Steps to reproduce
- Relevant logs or error messages

### Suggesting Features
Open an issue with the `enhancement` label. Good feature requests explain:
- The problem you're trying to solve
- Why it belongs in SmartSearch's core

### Submitting a Pull Request
1. Fork the repo
2. Create a branch: `git checkout -b your-feature-name`
3. Make your changes
4. Make sure the build passes: `mvn clean compile`
5. Commit with a clear message: `git commit -m "feat: add X"`
6. Push and open a PR against `main`

## Good First Issues
Look for issues tagged `good first issue`. Areas that always welcome contributions:
- Additional failure scenario tests
- Metrics and observability improvements
- Documentation improvements
- API endpoint enhancements
- DLQ handling improvements

## Code Style
- Follow standard Java conventions
- Use Spring Boot patterns already in the codebase
- Add comments for non-obvious logic
- Keep correctness guarantees intact — don't break the failure matrix

## Commit Message Format
Use conventional commits:
- `feat:` — new feature
- `fix:` — bug fix
- `docs:` — documentation only
- `refactor:` — code change that neither fixes a bug nor adds a feature
- `test:` — adding or updating tests
- `obs:` — observability improvements (metrics, logs, dashboards)

## Questions?
Open an issue or start a discussion — happy to help you get oriented.
