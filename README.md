# rag-dealer

Spring Boot based multi-module application demonstrating a simple HTTP service.

## Requirements
- Java 21
- Gradle 8+
- Docker

## Build

```bash
gradle :app:build
```

## Run locally

```bash
gradle :app:bootRun
```

Then verify:

```bash
curl http://localhost:8080/
```

## OpenAPI client generation

- Put your OpenAPI spec at `openapi/gigachat.yml` (or adjust the path in `app/build.gradle.kts`).
- Commands:
  - Generate only: `gradle :app:generateGigachatClient`
  - Build with generation: `gradle :app:build`

## Docker

```bash
docker build -t rag-dealer-chat:local -f app/Dockerfile .
docker run --rm -p 8080:8080 --env-file .env.prod rag-dealer-chat:local
```

All source code follows the guidelines outlined in `AGENTS.md`.
