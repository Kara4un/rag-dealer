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

## Agentic flow

The chat controller delegates each user message to a simple agent composed of
actions such as validation, token management and LLM invocation. The agent uses
a custom Spring AI `ChatModel` for GigaChat. Model parameters can be configured
under `spring.ai.gigachat.*` in `app/src/main/resources/application.yml`.

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

## Embeddings

The project provides a basic client for GigaChat embeddings. Create a
`GigaChatEmbeddingsClient` with the generated OpenAPI `DefaultApi` and call
`embed` with a list of texts:

```java
EmbeddingsClient client = new GigaChatEmbeddingsClient(api, "Embeddings", 64, 3,
        Duration.ofSeconds(10));
List<float[]> vectors = client.embed(List.of("first", "second"));
```

Supported models: `Embeddings` and `EmbeddingsGigaR`. Vector dimensionality
depends on the selected model (see official documentation). The client batches
requests and retries transient errors with exponential backoff.

### Temporary SSL bypass (dev only)

If you see PKIX/SSL handshake errors when calling external HTTPS endpoints and need a temporary workaround for local development, set the property below to trust all server certificates for Spring WebClient:

```
http.client.ssl.insecure-trust-all-certificates: true
```

This is already enabled in `app/src/main/resources/application-local.yml`. Never enable this in production.

All source code follows the guidelines outlined in `AGENTS.md`.
