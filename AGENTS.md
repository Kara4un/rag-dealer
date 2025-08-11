# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java`: Production Java sources (packages like `com.yourorg.ragdealer`).
- `src/main/resources`: Non-code assets (configs, prompts, templates).
- `src/test/java`: Unit tests mirroring main package layout.
- `docs/`: Design notes and architecture diagrams (optional).
- `scripts/`: Local tooling and helper scripts (optional).
- Root files: `README.md`, `LICENSE`. Add `pom.xml` (Maven) or `build.gradle[.kts]` (Gradle) at the root.

## Build, Test, and Development Commands
Use the tool that matches this repo (add one of the files above):
- Maven
  - `mvn clean verify`: Full build + tests.
  - `mvn test`: Run unit tests.
  - `mvn package -DskipTests`: Build an artifact fast.
- Gradle
  - `./gradlew build`: Full build + tests.
  - `./gradlew test`: Run unit tests.
  - `./gradlew jar`: Build a JAR (if configured).

## Coding Style & Naming Conventions
- Indentation: 4 spaces; max line length 120.
- Packages: lowercase reversed domain, e.g., `com.yourorg.ragdealer.index`.
- Classes: PascalCase (`RagDealerService`), methods/fields: camelCase, constants: UPPER_SNAKE_CASE.
- Imports: avoid wildcards; keep ordering consistent.
- Formatters/linters: If configured, run `mvn fmt:format` or `./gradlew spotlessApply`. Prefer Google Java Style.

## Testing Guidelines
- Framework: JUnit 5; use Mockito for stubs/mocks when needed.
- Location: Mirror production package structure under `src/test/java`.
- Naming: `ClassNameTest` for unit tests; prefer one behavior per test.
- Running: `mvn test` or `./gradlew test` (see above). Add high-value integration tests under `src/test/java` with `*IT` suffix if applicable.

## Commit & Pull Request Guidelines
- Branches: `feature/<short-title>`, `fix/<issue-id>`, e.g., `feature/indexer-cache`.
- Commits: Clear, imperative subject (≤72 chars). Conventional Commits are welcome: `feat:`, `fix:`, `docs:`, `test:`, `refactor:`.
- PRs: Include purpose, scope, screenshots/logs if useful, and linked issues. Describe testing steps and any follow-up tasks.

## Security & Configuration Tips
- JDK: Use LTS (17 or 21). Specify via `.java-version` or toolchain.
- Secrets: Never commit API keys; use environment variables or a local `.env` ignored by Git.
- Reproducibility: Pin plugin and dependency versions in your build file.

