# Cognara Backend

> Milestone 1 — Backend Foundation

Cognara is a multi-tenant AI knowledge and customer-support platform.
This module contains the Java/Spring Boot backend.

---

## Prerequisites

| Tool | Required Version | Notes |
|------|-----------------|-------|
| Java (JDK) | 21 or higher | [Eclipse Temurin](https://adoptium.net/) recommended |
| Maven | 3.9+ | Or use the included `mvnw` wrapper — no install needed |
| PostgreSQL | 15 or higher | Tested with PostgreSQL 18 |

---

## Database Setup

Open a terminal with `psql` available (or use pgAdmin).

```sql
-- 1. Create a dedicated database user
CREATE USER cognara_user WITH PASSWORD 'your_strong_password';

-- 2. Create the database
CREATE DATABASE cognara OWNER cognara_user;

-- 3. Grant privileges
GRANT ALL PRIVILEGES ON DATABASE cognara TO cognara_user;
```

On Windows, `psql` is installed at:
```
C:\Program Files\PostgreSQL\18\bin\psql.exe -U postgres
```

---

## Local configuration

Copy `application-local.properties.example` to `application-local.properties` in
the project root. The real local file is ignored by Git, so passwords and the JWT
signing key remain outside version control without using environment variables.

---

## Running the Tests

The unit tests use `@WebMvcTest`, which loads only the web layer.
They **do not require a database connection** and can be run at any time.

```powershell
# Using the Maven wrapper (no Maven install needed)
.\mvnw test

# Or if Maven is on your PATH
mvn test
```

Expected output:
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Running the Application

Create `application-local.properties` as described above, then:

```powershell
# 1. Package and run
.\mvnw spring-boot:run

# Or build a JAR first, then run it
.\mvnw package -DskipTests
java -jar target\cognara-backend-0.0.1-SNAPSHOT.jar
```

The server starts on port **8080** by default.

---

## Verifying It Works

Once the application is running:

```powershell
# Custom health endpoint
Invoke-RestMethod http://localhost:8080/api/health

# Spring Actuator health (includes DB connection check)
Invoke-RestMethod http://localhost:8080/actuator/health
```

Expected response from `/api/health`:
```json
{
  "status": "UP",
  "application": "cognara-backend",
  "timestamp": "2026-10-19T11:30:00Z"
}
```

---

## Project Structure

```
COGNARA/
├── pom.xml                          # Maven build descriptor
├── mvnw / mvnw.cmd                  # Self-contained Maven wrapper
├── src/
│   ├── main/
│   │   ├── java/com/cognara/
│   │   │   ├── CognaraApplication.java       # Spring Boot entry point
│   │   │   ├── common/                       # Shared utilities & exception handling
│   │   │   │   └── exception/
│   │   │   │       ├── ApiErrorResponse.java
│   │   │   │       ├── DuplicateResourceException.java
│   │   │   │       ├── ResourceNotFoundException.java
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── company/                      # Multi-tenant Company domain
│   │   │   │   ├── Company.java              # Tenant entity (@Entity)
│   │   │   │   ├── CompanyController.java    # REST API endpoints (@RestController)
│   │   │   │   ├── service/
│   │   │   │   │   └── CompanyService.java   # Business logic & uniqueness (@Service)
│   │   │   │   ├── repository/
│   │   │   │   │   └── CompanyRepository.java # Data repository (@Repository)
│   │   │   │   └── dto/
│   │   │   │       ├── CreateCompanyRequest.java
│   │   │   │       └── CompanyResponse.java
│   │   │   ├── user/                         # User domain (tenant-scoped)
│   │   │   │   ├── User.java                 # User entity with company FK (@Entity)
│   │   │   │   ├── repository/
│   │   │   │   │   └── UserRepository.java   # Tenant-isolated queries (@Repository)
│   │   │   │   ├── service/                  # Future user services
│   │   │   │   └── dto/                      # Future user DTOs
│   │   │   ├── config/                       # App-wide configuration beans
│   │   │   └── health/
│   │   │       └── HealthController.java     # GET /api/health (@RestController)
│   │   └── resources/
│   │       └── application.properties        # Main config (PostgreSQL, env-vars)
│   └── test/
│       ├── java/com/cognara/
│       │   ├── company/
│       │   │   ├── CompanyControllerTest.java # WebMvc slice tests (validation, errors)
│       │   │   └── service/
│       │   │       └── CompanyServiceTest.java # Service unit tests
│       │   ├── user/
│       │   │   └── UserRepositoryTest.java    # DataJpaTest (composite uniqueness)
│       │   └── health/
│       │       └── HealthControllerTest.java  # Health endpoint test
│       └── resources/
│           └── application-test.properties    # H2 in-memory test configuration
├── architecture.md                           # System architecture documentation
└── context.md                                # Project context, status, and tasks
```

---

## API Reference

### `GET /api/health`
Returns a lightweight JSON confirming the application is running.

### `POST /api/companies`
Creates a new tenant organization.

**Request Body:**
```json
{
  "name": "Acme Corporation",
  "slug": "acme-corp"
}
```

**Response `201 Created`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Acme Corporation",
  "slug": "acme-corp",
  "createdAt": "2026-10-19T12:00:00Z"
}
```

**Errors:**
- `400 Bad Request`: If `name` or `slug` is blank, or `slug` format is invalid.
- `409 Conflict`: If a company with the given `slug` already exists.

### `GET /api/companies/{id}`
Retrieves company details by UUID. Requires a Bearer token for the same company.

**Response `200 OK`:**
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Acme Corporation",
  "slug": "acme-corp",
  "createdAt": "2026-10-19T12:00:00Z"
}
```

**Errors:**
- `401 Unauthorized`: If the request has no valid Bearer token.
- `403 Forbidden`: If the token belongs to another company.

### `POST /api/auth/login`
Authenticates a user within a company and returns a signed JWT. The same email may
exist for another company, so `companyId` is required.

```json
{
  "companyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "support@acme.test",
  "password": "your-password"
}
```

The response contains `token` and `tokenType` (`Bearer`). Send it as
`Authorization: Bearer <token>` for protected endpoints.

### `POST /api/users`
Creates a user in the authenticated user's company. The request `companyId` must
match the `companyId` claim in the JWT; a token from another tenant receives 403.

### `GET /api/users/{id}`
Retrieves a user only when that user belongs to the JWT's company. A cross-tenant
lookup returns 404 to avoid exposing the other tenant's user record.

## Security configuration

Copy `application-local.properties.example` to `application-local.properties`, then
set the database and JWT values in that local, gitignored file:

```properties
cognara.local.db-url=jdbc:postgresql://localhost:5432/cognara
cognara.local.db-username=postgres
cognara.local.db-password=your_database_password
cognara.local.jwt-secret=a-random-secret-with-at-least-32-bytes
```

JWTs are stateless HMAC-SHA256 tokens containing the user ID (`sub`), email, and
company ID. Invalid, expired, or malformed tokens never populate the security context.
There are deliberately no refresh tokens in this milestone.

Company registration remains public. Since this model does not yet contain roles or
an atomic company-plus-owner registration flow, provisioning the first user for a
new company remains an operational bootstrap step. Do not relax the protected user
creation endpoint to solve that gap; add an explicit ownership/registration design
in a later milestone.

---

## What's Not Implemented Yet

- Roles/permissions and a safe first-owner registration flow
- Refresh-token/session revocation support
- Document upload and management
- AI / embedding / vector search
- Frontend
