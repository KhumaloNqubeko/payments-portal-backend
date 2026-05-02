# International Payments Portal

This solution contains a secure Spring Boot backend in `payments-portal-backend` and a React + Vite frontend in `../payments-portal-frontend`.

## Stack

- Frontend: React + Vite
- Backend: Spring Boot 3, Spring Security, Spring Data JPA, Hibernate
- Database: PostgreSQL
- Password hashing: BCrypt
- Validation: Jakarta Bean Validation with allowlist regex patterns
- Testing: MockMvc integration tests
- CI/CD: GitHub Actions

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
2. Use `.env.example` as a reference and set the same values in your shell, IDE run configuration, or deployment environment.
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

## Environment variables / properties needed

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
- BCrypt automatically salts each password and is intentionally slow, which helps defend against brute-force attacks.
- Strong password policy enforced on the frontend and backend.
- Allowlist regex validation for registration and international payment fields.
- JWT-based stateless authentication with RBAC for `CUSTOMER` and `EMPLOYEE`.
- Protected frontend routes redirect unauthenticated users to login.
- Customer endpoints are restricted to the authenticated customer’s own records to prevent IDOR.
- DTO-based request mapping helps prevent mass-assignment style attacks.
- Spring Data JPA repositories and parameterised queries reduce SQL injection risk.
- React output escaping plus backend validation help reduce XSS risk.
- CSP, HSTS, frame denial, referrer policy, and permissions policy headers are configured.
- CORS is restricted to the configured trusted frontend origin.
- Login throttling slows repeated brute-force login attempts.
- Sensitive account values are masked in API responses and not logged in audit entries.
- HTTPS/TLS settings are driven by environment variables and `requiresSecure()` can enforce transport security.
- Session hijacking risk is reduced by using stateless JWT authentication together with HTTPS.
- CSRF is disabled only because this implementation uses stateless JWT bearer tokens instead of cookie-backed server sessions.
- Secrets such as DB password, JWT secret, and SSL keystore password are loaded from environment variables instead of being committed in the active config.

## DevSecOps pipeline

The repository includes a GitHub Actions pipeline that runs on pushes and pull requests. It performs:

- secret scanning with Gitleaks
- backend Maven test execution with the test profile
- backend Spring Boot package build

The frontend repository also includes its own GitHub Actions pipeline for:

- secret scanning with Gitleaks
- dependency installation
- `npm audit`
- production build validation

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
- HTTPS redirection is configurable but requires a real certificate and keystore for production.
- The sample login throttling is in-memory and should be replaced by shared infrastructure for multi-node deployment.

## Tools and Ethical Disclosure

- AI tool used: ChatGPT
- Security framework used: Spring Security with `BCryptPasswordEncoder`
- Spring Security and BCrypt are safer than writing custom authentication because they reduce the risks of weak hashing, missing salts, broken sessions, insecure login handling, and other common implementation mistakes.
- AI assistance was used to speed up scaffolding, explanation, and refinement, but the developer still reviewed, tested, and configured the final solution.
