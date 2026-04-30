# International Payments Portal

This solution contains a secure Spring Boot backend in [`payments-portal-backend`](.) and a React + Vite frontend in [`../payments-portal-frontend`](../payments-portal-frontend).

## Stack

- Frontend: React + Vite
- Backend: Spring Boot 3, Spring Security, Spring Data JPA, Hibernate
- Database: PostgreSQL
- Password hashing: BCrypt
- Validation: Jakarta Bean Validation with allowlist regex patterns
- Testing: MockMvc integration tests

## Project structure

- `src/main/java/com/bank/paymentsportal/controller`
- `src/main/java/com/bank/paymentsportal/service`
- `src/main/java/com/bank/paymentsportal/repository`
- `src/main/java/com/bank/paymentsportal/entity`
- `src/main/java/com/bank/paymentsportal/dto`
- `src/main/java/com/bank/paymentsportal/security`
- `src/main/java/com/bank/paymentsportal/validation`
- `src/main/java/com/bank/paymentsportal/exception`
- `src/main/java/com/bank/paymentsportal/config`

## Setup instructions

1. Create a PostgreSQL database named `payments_portal`.
2. Copy `.env.example` to `.env` or set the same values in your shell environment.
3. Start the backend:

```bash
mvn spring-boot:run
```

4. In `../payments-portal-frontend`, copy `.env.example` to `.env` and set `VITE_API_BASE_URL=/api`.
5. Start the frontend:

```bash
npm install
npm run dev
```

## Environment variables / properties needed once you move to the environment

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

Frontend local HTTPS:

- `DEV_CERT_PASSPHRASE`

## Demo accounts

- Customer 1: `ama.dlamini` / `Cust0mer!Pass1`
- Customer 2: `thabo.naidoo` / `Cust0mer!Pass2`
- Employee: `employee.naledi` / `Employ3e!Pass1`

## Security controls implemented

- BCrypt password hashing with no plaintext password storage.
- Strong password policy enforced on the frontend and backend.
- Allowlist regex validation for customer registration and payment capture fields.
- JWT-based stateless authentication with RBAC for `CUSTOMER` and `EMPLOYEE`.
- Customer endpoints restricted to the authenticated customer’s own records to prevent IDOR.
- DTO-based request mapping to avoid mass assignment.
- Spring Data JPA repositories and parameter binding to reduce SQL injection risk.
- CSP, HSTS, frame denial, referrer policy, and permissions policy headers.
- CORS restricted to the configured trusted frontend origin.
- Login throttling filter to slow brute-force attempts.
- Sensitive fields are masked in API responses and not logged in audit entries.
- HTTPS/TLS-ready settings via environment variables and optional `requiresSecure()` enforcement.
- Secrets such as DB password, JWT secret, and SSL keystore password are loaded from environment variables instead of being committed in the active config.

## DevSecOps pipeline

The repository includes a GitHub Actions pipeline that runs on pushes and pull requests. It performs:

- secret scanning with Gitleaks
- frontend dependency install, audit, and production build
- backend Maven test execution with the test profile

## Testing

Run:

```bash
mvn test -Dspring.profiles.active=test
```

The integration tests cover:

- invalid SWIFT rejection
- invalid South African ID rejection
- hashed password storage
- blocking customer access to another customer’s transaction
- blocking employee endpoints for customers
- verify and submit status flow

## Known limitations

- JWT logout is client-side token disposal plus audit logging; there is no token revocation list.
- SWIFT integration is intentionally mocked as a status transition for the assignment.
- HTTPS redirection is configurable but requires a real certificate/keystore for production.
- The sample login throttling is in-memory and should be replaced by shared infrastructure for multi-node deployment.

## How this solution addresses the requirements

- Customer registration, login, payment capture, and transaction history are implemented in the React portal.
- Employee login, transaction review, verification, rejection, and SWIFT submission are implemented in the employee portal and API.
- Transactions are stored with `PENDING_VERIFICATION`, then transition to `VERIFIED`, `SUBMITTED_TO_SWIFT`, or `REJECTED`.
- Users, transactions, and audit logs are persisted with JPA entities.
- The backend uses a clean layered Spring Boot architecture matching the requested packages.
- Validation, security, RBAC, audit logging, documentation, demo accounts, and tests are all included.
