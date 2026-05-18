# Task 3 Employee International Payments Portal Backend

This backend supports a static-login international payments workflow for the Task 3 employee portal assignment.

## Stack

- Spring Boot 3
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL for local runtime
- H2 for tests and CI API checks
- JWT authentication
- BCrypt password hashing

## Task 3 behavior

- No public registration endpoint is exposed in the running application.
- Customer and employee users are pre-seeded by the system.
- Customers can log in and submit payment records.
- Employees can log in, review those records, verify them, reject them, or submit them to the mocked SWIFT step.

## Demo accounts

- Customer 1: `nonhlekhumzie` / `Cust0mer!Pass1`
- Customer 2: `bob` / `Cust0mer!Pass2`
- Employee: `nonokhumzie` / `Employ3e!Pass1`

## Run locally

```bash
mvn spring-boot:run
```

For tests:

```bash
mvn "-Dspring.profiles.active=test" test
```

For a local SonarQube scan after your server is running:

```bash
mvn -B -DskipTests verify sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token=YOUR_SONAR_TOKEN
```

## Environment variables

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MINUTES`
- `FRONTEND_URL`
- `SERVER_PORT`
- `REQUIRE_HTTPS`
- `SSL_ENABLED`
- `SSL_KEY_STORE`
- `SSL_KEY_STORE_PASSWORD`
- `SSL_KEY_STORE_TYPE`

## Security controls implemented

- BCrypt password hashing with automatic salting
- No plaintext password storage
- Regex allowlist validation on DTOs
- JWT authentication with role-based access control
- Employee endpoints restricted to `EMPLOYEE`
- Customer transaction access scoped to the owning customer
- Spring Data JPA repositories to reduce SQL injection risk
- CSP, HSTS, frame denial, referrer policy, and permissions policy headers
- CORS restricted to the configured frontend origin
- In-memory login throttling to slow brute-force attempts
- Masking of account data in API responses
- HTTPS/TLS configuration driven by environment variables

## DevSecOps pipeline

The root repository contains the lecturer-facing DevSecOps pipeline definitions:

- GitHub Actions: `../.github/workflows/task3-devsecops.yml`
- CircleCI: `../.circleci/config.yml`
- SonarQube project settings: `../sonar-project.properties`

The pipeline is designed to provide:

- secret scanning with Gitleaks
- static analysis with Semgrep
- Java code analysis with SpotBugs
- dependency vulnerability scanning with OWASP Dependency-Check and `npm audit`
- backend tests and package build
- frontend production build validation
- Newman API/security flow checks
- SonarQube scan support through environment-backed secrets

## Evidence included

- Postman/Newman collection: `postman/Payments-Portal.postman_collection.json`
- Demo script: `../DEMO_SCRIPT.md`
- Integration tests: `src/test/java/com/bank/paymentsportal/SecurityIntegrationTest.java`

## Tools and Ethical Disclosure

- AI tool used: ChatGPT / Codex
- Security framework used: Spring Security with `BCryptPasswordEncoder`
- Spring Security and BCrypt are safer than custom authentication because they reduce risks such as weak hashing, missing salts, broken sessions, and insecure login handling.

## Known limitations

- JWT logout clears local client state and writes an audit log, but it does not implement token revocation.
- The brute-force throttle is in-memory and would need shared storage for multi-node production.
- SonarQube execution in CI requires the repository secrets or CircleCI environment variables to be configured by the repository owner.
