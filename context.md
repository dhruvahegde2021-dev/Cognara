# Cognara — Project Context

## Objective

Build a multi-tenant AI knowledge and customer-support platform, learning backend engineering along the way.

## Current Milestone: 2 — Initial Multi-Tenant Domain Foundation

---

## Implemented Features

| Feature | Status |
|---------|--------|
| Maven project structure (`com.cognara`) | ✅ Done |
| Spring Boot 3.5.3 + Java 21 target | ✅ Done |
| PostgreSQL + Spring Data JPA wired up | ✅ Done |
| Spring Boot Actuator (`/actuator/health`) | ✅ Done |
| `GET /api/health` endpoint | ✅ Done |
| Environment-variable DB credentials | ✅ Done |
| `Company` entity (UUID, name, unique slug, createdAt) | ✅ Implemented & verified |
| `User` entity (UUID, name, email, passwordHash, Company FK) | ✅ Implemented & verified |
| Company-scoped email uniqueness constraint (`company_id, email`) | ✅ Implemented & verified |
| Spring Data JPA repositories (`CompanyRepository`, `UserRepository`) | ✅ Implemented & verified |
| DTOs (`CreateCompanyRequest`, `CompanyResponse`) without leaking credentials | ✅ Implemented & verified |
| Service layer (`CompanyService`) with slug uniqueness checks | ✅ Implemented & verified |
| Endpoints (`POST /api/companies`, `GET /api/companies/{id}`) | ✅ Implemented & verified |
| Global error handling (`GlobalExceptionHandler`, `ApiErrorResponse`) | ✅ Implemented & verified |
| Unit and integration test suite (12 passing tests) | ✅ Implemented & verified passing |
| Annotation-based Spring Boot architecture refactor | ✅ Verified & documented |
| Project documentation (`architecture.md`, `context.md`, `README.md`) | ✅ Updated |

---

## Important Source Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven build descriptor — Spring Boot 3.5.3 parent, Java 21, JPA, Validation, Actuator |
| `mvnw.cmd` / `mvnw` | Self-contained Maven Wrapper scripts |
| `src/main/resources/application.properties` | App configuration; PostgreSQL credentials via env vars |
| `src/main/java/com/cognara/CognaraApplication.java` | Spring Boot application entry point |
| `src/main/java/com/cognara/company/Company.java` | Tenant entity |
| `src/main/java/com/cognara/company/CompanyController.java` | REST endpoints: POST & GET `/api/companies` |
| `src/main/java/com/cognara/company/service/CompanyService.java` | Business logic, slug collision checks, DTO mapping |
| `src/main/java/com/cognara/company/repository/CompanyRepository.java` | Company JPA repository with slug lookups |
| `src/main/java/com/cognara/company/dto/CreateCompanyRequest.java` | Request validation record |
| `src/main/java/com/cognara/company/dto/CompanyResponse.java` | Safe response record |
| `src/main/java/com/cognara/user/User.java` | User entity with required company relationship |
| `src/main/java/com/cognara/user/repository/UserRepository.java` | Repository with company-scoped query methods |
| `src/main/java/com/cognara/common/exception/GlobalExceptionHandler.java` | Uniform error translations (400, 404, 409) |
| `src/test/java/com/cognara/company/CompanyControllerTest.java` | WebMvc endpoint test suite (5 tests) |
| `src/test/java/com/cognara/company/service/CompanyServiceTest.java` | Service unit test suite (4 tests) |
| `src/test/java/com/cognara/user/UserRepositoryTest.java` | DataJpaTest testing relationship & tenant-scoped uniqueness (2 tests) |
| `src/test/java/com/cognara/health/HealthControllerTest.java` | Health endpoint test (1 test) |

---

## Prerequisites

- Java 21+ (JDK 25 / Temurin installed at `C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot`)
- PostgreSQL 18 (installed at `C:\Program Files\PostgreSQL\18`)
- Maven: Self-contained wrapper (`mvnw.cmd`) included

---

## Environment Variables Required

```powershell
$env:DB_USERNAME = "cognara_user"
$env:DB_PASSWORD = "your_strong_password"
# DB_HOST defaults to localhost, DB_PORT to 5432, DB_NAME to cognara
```

---

## Commands

```powershell
# Run the full test suite (12 tests, uses H2 in-memory profile)
.\mvnw.cmd test

# Run the application (PostgreSQL must be running with schema configured)
.\mvnw.cmd spring-boot:run

# Build executable fat JAR
.\mvnw.cmd package -DskipTests
java -jar target\cognara-backend-0.0.1-SNAPSHOT.jar
```

---

## Known Issues / Limitations

- **Tenant Isolation Boundaries:** Cross-company user lookups are prevented at the data model (`company_id, email` unique index) and repository query level (`findByCompanyIdAndEmail`), but runtime authentication (JWT tokens, security context tenant extraction) is not yet implemented. Tenant isolation cannot be considered complete until authorization middleware is in place.
- **Database Schema Sync:** Production environment uses `ddl-auto=validate`. Initial database tables (`companies`, `users`) need to be created via migration or initial DDL script. Database migration tool (Flyway) is scheduled for the next milestone.

---

## Next Recommended Tasks (Milestone 3)

1. Introduce database migration management (Flyway or Liquibase) to manage versioned DDL for `companies` and `users`.
2. Implement tenant user management endpoints (e.g. creating users within a tenant with password hashing via BCrypt).
3. Implement JWT-based authentication and security context filters to enforce request-level tenant isolation.
