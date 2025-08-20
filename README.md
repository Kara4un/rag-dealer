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

### Temporary SSL bypass (dev only)

If you see PKIX/SSL handshake errors when calling external HTTPS endpoints and need a temporary workaround for local development, set the property below to trust all server certificates for Spring WebClient:

```
http.client.ssl.insecure-trust-all-certificates: true
```

This is already enabled in `app/src/main/resources/application-local.yml`. Never enable this in production.

All source code follows the guidelines outlined in `AGENTS.md`.
