# rag-dealer

Micronaut-based multi-module application demonstrating a simple HTTP service.

## Requirements
- Java 21
- Docker

## Build

```bash
./gradlew :app:build
```

## Docker

```bash
docker build -t rag-dealer:local -f app/Dockerfile .
docker run --rm -p 8080:8080 rag-dealer:local
```

Then verify:

```bash
curl http://localhost:8080/
```

All source code follows the guidelines outlined in `AGENTS.md`.
