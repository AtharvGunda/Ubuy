# Core Engineering Guardrails (Karpathy + Ponytail)

1. **Think Before Coding**: State assumptions explicitly before writing code. If requirements are ambiguous, stop and ask clarifying questions instead of guessing.
2. **Simplicity First (Ponytail Ladder)**: Before writing custom abstractions or adding new libraries, check if the feature is needed, if existing project code already solves it, or if standard library / native APIs handle it.
3. **Surgical Changes**: Touch only the files and lines required for the task. Do not refactor unrelated code or leave orphaned imports.
4. **Goal-Driven Execution**: Define clear verification steps (tests, build checks, or browser verification) and iterate until every check passes.


## Ubuy Tech Stack & Architecture
- **Stack**: Java 17+, Spring Boot 3.x, Spring Web (MVC), Spring Security, Spring Data JPA (Hibernate), MySQL.
- **Architecture**: Strict Controller -> Service -> Repository -> Entity layering using constructor Dependency Injection and DTOs.
- **Verification**: Run `./mvnw test` (or `mvn test`) and verify REST endpoints via curl/HTTP tests before completing any task.
- **Ponytail Rule**: Avoid unnecessary third-party libraries (no MapStruct, no complex microservices, no Redis unless requested). Keep configuration clean in `application.properties`.
