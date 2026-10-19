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

## Environment Variables

The application reads database credentials from environment variables.
**Never hardcode secrets in source files.**

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | PostgreSQL host | `localhost` (default) |
| `DB_PORT` | PostgreSQL port | `5432` (default) |
| `DB_NAME` | Database name | `cognara` (default) |
| `DB_USERNAME` | Database user | `cognara_user` |
| `DB_PASSWORD` | Database password | `your_strong_password` |

### Setting variables for the current PowerShell session

```powershell
$env:DB_USERNAME = "cognara_user"
$env:DB_PASSWORD = "your_strong_password"
# DB_HOST, DB_PORT, DB_NAME default to localhost:5432/cognara if not set
```

### Setting variables permanently (Windows)

```powershell
[System.Environment]::SetEnvironmentVariable("DB_USERNAME", "cognara_user", "User")
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_strong_password", "User")
```

> **Tip:** Open a new terminal after setting permanent variables for them to take effect.

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

Make sure the environment variables are set (see above), then:

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
Retrieves company details by UUID.

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
- `404 Not Found`: If no company exists with the given ID.

---

## What's Not Implemented Yet

- User management endpoints & registration flows
- Authentication & JWT token issuance
- Security filters for runtime tenant context resolution
- Document upload and management
- AI / embedding / vector search
- Frontend
