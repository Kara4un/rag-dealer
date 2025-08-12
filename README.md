# rag-dealer

Micronaut-based multi-module application demonstrating a simple HTTP service.

## Requirements
- Java 21
- Docker

## Build

```bash
./gradlew :app:build
```

## OpenAPI client generation

- Put your OpenAPI spec at `openapi/gigachat.yml` (or adjust the path in `app/build.gradle.kts`).
- Generated Micronaut HTTP client lives under `app/build/generated/openapi` and is added to the `main` source set automatically.
- Commands:
  - Generate only: `./gradlew :app:generateGigachatClient`
  - Build using generated code: `./gradlew :app:build` (generation runs before `compileJava`).

Notes:
- The generator used is `org.openapi.generator` with `generatorName=micronaut`.
- Packages:
  - APIs: `ru.kara4un.ragdealer.gigachat.api`
  - Models: `ru.kara4un.ragdealer.gigachat.model`
  - Invoker: `ru.kara4un.ragdealer.gigachat.invoker`

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

## TLS/SSL Troubleshooting (PKIX path building failed)

If you see `SSLHandshakeException: PKIX path building failed` when calling GigaChat/OAuth endpoints, it means the server's certificate chain isn't trusted by your JVM.

Options:

1) Local dev quick fix (trust-all; not for prod):

```bash
MICRONAUT_ENVIRONMENTS=local ./gradlew :app:run
```

This enables `app/src/main/resources/application-local.yml` with `micronaut.http.client.ssl.insecure-trust-all-certificates=true`.

2) Proper fix (recommended): create and configure a truststore with the provider's CA:

- Export the server certificate chain (via browser or OpenSSL), then create a PKCS#12 truststore:

```bash
# Example with a PEM file sber-ngw.pem you exported
keytool -importcert -alias sber-ngw -keystore sber-truststore.p12 \
  -storetype PKCS12 -storepass changeit -file sber-ngw.pem -noprompt
```

- Place the truststore at `app/src/main/resources/certs/sber-truststore.p12` (create the folder).

- Configure Micronaut HTTP client SSL in `application.yml` (or via env vars):

```yaml
micronaut:
  http:
    client:
      ssl:
        enabled: true
        trust-store:
          path: classpath:certs/sber-truststore.p12
          type: PKCS12
          password: ${TRUSTSTORE_PASSWORD:changeit}
```

- Docker: the classpath truststore is bundled automatically with the JAR, no extra flags needed. Alternatively, mount a truststore and set `MICRONAUT_CONFIG_FILES` or JVM `-D` props.

Do not use the trust-all option in production.
