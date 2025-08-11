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
docker build -t rag-dealer-chat:local -f app/Dockerfile .
docker run --rm -p 8080:8080 --env-file .env.prod rag-dealer-chat:local
```

Then verify:

```bash
curl http://localhost:8080/
```

All source code follows the guidelines outlined in `AGENTS.md`.

## GigaChat Chat

### Local Run

```bash
export $(cat .env.prod | xargs) && ./gradlew run
```

### Docker

```bash
docker build -t rag-dealer-chat:local -f app/Dockerfile .
docker run --rm -p 8080:8080 --env-file .env.prod rag-dealer-chat:local
```

Open [http://localhost:8080](http://localhost:8080) and start chatting.
