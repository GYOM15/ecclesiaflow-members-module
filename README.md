# EcclesiaFlow Members Module

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot 3.5.5](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Keycloak](https://img.shields.io/badge/Keycloak-23.0-blue.svg)](https://www.keycloak.org/)
[![gRPC 1.65.1](https://img.shields.io/badge/gRPC-1.65.1-blue.svg)](https://grpc.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Member management module for the **EcclesiaFlow** multi-tenant church management platform.
Handles registration, email confirmation, profile management, social login onboarding, and
bi-directional gRPC communication with the authentication module.

---

## Tech Stack

| Layer          | Technology                                       |
|----------------|--------------------------------------------------|
| Language       | [Java 21](https://openjdk.java.net/projects/jdk/21/) |
| Framework      | [Spring Boot 3.5.5](https://spring.io/projects/spring-boot), [Spring Security 6](https://docs.spring.io/spring-security/reference/) (OAuth2 Resource Server) |
| Identity       | [Keycloak 23.0](https://www.keycloak.org/) (OAuth2 / OIDC) |
| Inter-module   | [gRPC 1.65.1](https://grpc.io/) / [Protobuf 4.28.2](https://protobuf.dev/) |
| Resilience     | [Resilience4j 2.1.0](https://resilience4j.readme.io/) (Circuit Breaker + Retry) |
| Persistence    | [MySQL 9.0](https://dev.mysql.com/doc/refman/9.0/en/), [JPA](https://jakarta.ee/specifications/persistence/), [MapStruct 1.5.5](https://mapstruct.org/) |
| API-first      | [OpenAPI Generator 7.15.0](https://openapi-generator.tech/) |
| Documentation  | [SpringDoc 2.8.12](https://springdoc.org/) |
| Quality        | [JaCoCo](https://www.jacoco.org/jacoco/) (90% minimum coverage), 521 tests |

---

## Architecture

The module follows **Clean Architecture** with Ports & Adapters.
Business logic has zero framework dependency; all I/O goes through ports.

```
src/main/java/com/ecclesiaflow/
├── application/               # Spring config, event handlers, AOP logging
│   ├── config/                # Async, gRPC, Resilience, WebClient, OpenAPI
│   ├── handlers/              # MemberRegistrationEventHandler, MemberActivationEventHandler
│   └── logging/               # Structured logging aspects (per layer) + SecurityMaskingUtils
├── business/                  # Pure domain — no Spring imports
│   ├── domain/                # Member, MemberConfirmation, MemberStatus, ports
│   ├── services/              # MemberService, MemberConfirmationService
│   ├── security/              # ScopeValidationAspect, RoleToScopeMapper
│   └── exceptions/            # Domain exceptions (MemberNotFoundException, etc.)
├── io/                        # Infrastructure adapters
│   ├── communication/email/   # EmailGrpcClient (implements EmailClient port)
│   ├── grpc/client/           # AuthGrpcClient (outbound: Members → Auth)
│   ├── grpc/server/           # MembersGrpcServiceImpl (inbound: Auth → Members)
│   └── persistence/           # JPA entities, MapStruct mappers, repositories
└── web/                       # REST layer
    ├── controller/            # MembersController, MembersConfirmationController
    ├── delegate/              # Delegates (orchestrate service calls)
    ├── exception/             # GlobalExceptionHandler, ApiErrorResponse
    ├── mappers/               # OpenAPI model mappers
    └── security/              # AuthenticatedUserService, KeycloakJwtConverter, SecurityConfig
```

### Key Design Patterns

- **API-First**: OpenAPI spec (`src/main/resources/api/members.yaml`) generates controllers and DTOs
- **Delegate Pattern**: Controllers implement generated interfaces, delegate to `*Delegate` classes
- **Ports & Adapters**: Business layer defines interfaces; `io/` provides implementations
- **Event-Driven**: Registration publishes `MemberRegisteredEvent`; handler sends confirmation email asynchronously
- **gRPC Primary / WebClient Fallback**: Inter-module communication with graceful degradation

---

## Member Lifecycle

```
PENDING ──confirm email──▸ CONFIRMED ──set password──▸ ACTIVE
                                                         │
                                                    SUSPENDED
                                                         │
                                                      INACTIVE
```

| Status      | Description                                           |
|-------------|-------------------------------------------------------|
| `PENDING`   | Registered, awaiting email confirmation               |
| `CONFIRMED` | Email confirmed, password not yet set                 |
| `ACTIVE`    | Fully active account (also set directly for social login) |
| `SUSPENDED` | Temporarily suspended by admin                        |
| `INACTIVE`  | Deactivated account                                   |

---

## Request Flows

### Standard Registration

```
Client ──POST /members──▸ MembersController
                            └─▸ MembersManagementDelegate
                                  └─▸ MemberService.registerMember()
                                        ├─▸ Save member (status: PENDING)
                                        └─▸ Publish MemberRegisteredEvent
                                              └─▸ EmailGrpcClient.sendConfirmationEmail() [async]
                         ◂── 201 Created
```

### Email Confirmation → Password Setup

```
Client ──GET /members/confirmation?token=uuid──▸ MembersConfirmationController
                                                   └─▸ MemberConfirmationDelegate
                                                         ├─▸ Validate token (one-time use, 24h expiry)
                                                         ├─▸ Member: PENDING → CONFIRMED
                                                         └─▸ AuthGrpcClient.generateTemporaryToken()
                                                  ◂── 200 { temporaryToken, passwordEndpoint }

Client ──POST /auth/password/setup──▸ Auth Module (sets password, CONFIRMED → ACTIVE)
```

### Social Login Onboarding

```
Client ──POST /members/social-onboarding──▸ MembersController
         (Bearer: Keycloak JWT)              └─▸ SocialOnboardingDelegate
                                                   ├─▸ Extract keycloakUserId + email from JWT
                                                   ├─▸ Validate email matches JWT
                                                   └─▸ MemberService.registerSocialMember()
                                                         └─▸ Save member (status: ACTIVE, no email confirmation)
                                              ◂── 201 Created
```

---

## API

### Public Endpoints (No Authentication)

| Method | Path | Description |
|--------|------|-------------|
| POST   | `/ecclesiaflow/members` | Register new member |
| GET    | `/ecclesiaflow/members/confirmation` | Confirm account via email link (`?token=uuid`) |
| POST   | `/ecclesiaflow/members/new-confirmation` | Resend confirmation link |
| GET    | `/ecclesiaflow/members/{email}/confirmation-status` | Check confirmation status |

### Authenticated Endpoints (Bearer JWT)

| Method | Path | Scope | Description |
|--------|------|-------|-------------|
| GET    | `/ecclesiaflow/members/me` | `ef:members:read:own` | Get own profile |
| PATCH  | `/ecclesiaflow/members/me` | `ef:members:write:own` | Update own profile |
| DELETE | `/ecclesiaflow/members/me` | `ef:members:delete:own` | Delete own account |
| POST   | `/ecclesiaflow/members/social-onboarding` | (authenticated) | Onboard social login user |
| GET    | `/ecclesiaflow/members` | `ef:members:read:all` | List all members (paginated) |
| GET    | `/ecclesiaflow/members/{memberId}` | `ef:members:read:all` | Get member by ID |
| PATCH  | `/ecclesiaflow/members/{memberId}` | `ef:members:write:all` | Update member |
| DELETE | `/ecclesiaflow/members/{memberId}` | `ef:members:delete:all` | Delete member |

### Query Parameters (GET /members)

| Parameter   | Type      | Default     | Description                    |
|-------------|-----------|-------------|--------------------------------|
| `page`      | integer   | `0`         | Page number (0-indexed)        |
| `size`      | integer   | `20`        | Items per page (max 100)       |
| `search`    | string    | -           | Search by name or email        |
| `status`    | string    | -           | Filter by member status        |
| `sort`      | string    | `createdAt` | Sort field                     |
| `direction` | string    | `DESC`      | Sort direction: `ASC` or `DESC`|

### Error Responses

| Code | Reason |
|------|--------|
| 400  | Validation error or malformed request |
| 401  | Missing or invalid JWT |
| 403  | Insufficient scopes |
| 404  | Member or token not found |
| 409  | Email or account already exists |
| 410  | Confirmation token expired |
| 500  | Internal server error |

### API Documentation

- **Swagger UI** — http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON** — http://localhost:8080/v3/api-docs
- **Contract source** — `src/main/resources/api/members.yaml`

---

## Security

### Keycloak OAuth2

The module is an **OAuth2 Resource Server** validating JWTs issued by Keycloak.
No local JWT parsing or shared secrets — validation uses Keycloak's public JWKS endpoint.

`AuthenticatedUserService` extracts identity from the JWT:

| Method | Source | Purpose |
|--------|--------|---------|
| `getKeycloakUserId()` | `sub` claim | Primary user identifier |
| `getEmail()` | `email` claim | User email address |
| `getRoles()` | Spring authorities | Keycloak roles (ROLE_USER, etc.) |
| `getScopes()` | `scope` claim | OAuth2 scopes |
| `isEmailVerified()` | `email_verified` claim | Email verification status |

`KeycloakJwtConverter` extracts roles from multiple JWT locations:
1. Direct `roles` claim
2. `realm_access.roles` (realm-level)
3. `resource_access.{client}.roles` (client-level)

### Scope Validation

Endpoints are protected by `@RequireScopes` annotations, validated by `ScopeValidationAspect` (AOP).

Scopes are derived from two sources (union):
- **JWT scopes** — `scope` claim (when Keycloak Authorization Services is configured)
- **Role mapping** — `RoleToScopeMapper` converts Keycloak roles to scopes:

| Role          | Scopes |
|---------------|--------|
| `USER`        | `read:own`, `write:own`, `delete:own` |
| `ADMIN`       | All scopes (`read/write/delete` for `own` + `all`) |
| `SUPER_ADMIN` | All scopes |

Toggle: `ECCLESIAFLOW_SCOPES_ENABLED=false` disables scope validation (useful when Keycloak scopes are not yet configured).

---

## gRPC Services

Proto files are in `src/main/proto/`. Classes are generated during `mvn generate-sources`.

### Inbound (this module exposes — port 9091)

| Proto | Service | RPC | Called by |
|-------|---------|-----|-----------|
| `members_service.proto` | `MembersService` | `GetMemberConfirmationStatus` | Auth module |
| `members_service.proto` | `MembersService` | `NotifyAccountActivated` | Auth module |

### Outbound (this module calls)

| Proto | Service | RPC | Target | Port |
|-------|---------|-----|--------|------|
| `auth_service.proto` | `AuthService` | `GenerateTemporaryToken` | Auth module | 9090 |
| `email_service.proto` | `EmailService` | `SendEmail` | Email service | 9092 |

### Email Service Resilience

The `EmailGrpcClient` is protected by Resilience4j:

- **Circuit Breaker**: Opens after 50% failure rate (sliding window of 10 calls), 30s recovery
- **Retry**: 3 attempts with 500ms delay for transient failures
- **Graceful degradation**: Registration succeeds even if the email service is down

---

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **MySQL 9.0+** (or 8.0+ compatible)
- **Keycloak** running (shared with Auth module)
- **Auth module** running on port 8081

---

## Quick Start

### 1. Configure environment

```bash
cp .env.example .env
```

Key variables to fill in:

| Variable | Description |
|----------|-------------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL connection |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials |
| `KEYCLOAK_ISSUER_URI` | Keycloak realm issuer URL |
| `KEYCLOAK_JWKS_URI` | Keycloak JWKS endpoint |
| `GRPC_AUTH_HOST` / `GRPC_AUTH_PORT` | Auth module gRPC address |
| `GRPC_EMAIL_HOST` / `GRPC_EMAIL_PORT` | Email service gRPC address |

See `.env.example` for the full list with defaults.

### 2. Build and run

```bash
mvn clean generate-sources     # compile + generate OpenAPI & Protobuf
mvn test                       # run 521 tests + 90% coverage check
mvn spring-boot:run            # start on port 8080
```

### 3. Verify

```bash
curl http://localhost:8080/actuator/health     # health check
open http://localhost:8080/swagger-ui/index.html  # API docs
```

> **IntelliJ**: generated sources are automatically added via `build-helper-maven-plugin`.
> If not, mark `target/generated-sources/openapi/src/main/java` as Generated Sources Root.

---

## Configuration

All configuration is driven by environment variables (loaded via [spring-dotenv](https://github.com/paulschwarz/spring-dotenv)).

- `application.properties` — Base configuration with dev-friendly defaults
- `application-prod.properties` — Production overrides (activate with `SPRING_PROFILES_ACTIVE=prod`)
- `.env` — Environment-specific values (never committed)

### Key Sections

**Database**: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

**Keycloak OAuth2**: `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_JWKS_URI`

**gRPC**: `GRPC_ENABLED`, `GRPC_SERVER_PORT` (9091), `GRPC_AUTH_HOST/PORT` (Auth), `GRPC_EMAIL_HOST/PORT` (Email)

**CORS**: `CORS_ALLOWED_ORIGINS` (default: `localhost:3000,4200,5173`)

**Scopes**: `ECCLESIAFLOW_SCOPES_ENABLED` (default: `true`)

**Frontend**: `FRONTEND_BASE_URL` (for confirmation email links)

### Production Profile

Activate with `SPRING_PROFILES_ACTIVE=prod`. Overrides:

- `ddl-auto=validate` (no schema changes)
- HikariCP pool tuning (20 max connections)
- Reduced logging (WARN for Spring, INFO for app)
- Kubernetes health probes enabled
- Swagger cache enabled
- Error details hidden

---

## Testing

521 tests covering all layers, enforced at 90% line and branch coverage.

```bash
mvn test                              # unit + integration tests + coverage gate
mvn clean test jacoco:report          # with HTML report
open target/site/jacoco/index.html    # view report
```

| Category    | Scope |
|-------------|-------|
| Unit        | Domain objects, services, delegates, controllers, mappers |
| Integration | gRPC clients/server, security config, JPA repositories |
| AOP         | 8 logging aspects (REST, gRPC, business, email, security) |
| Exception   | GlobalExceptionHandler, error models |

Test database: **H2 in-memory** (overrides MySQL via `src/test/resources/application.properties`).

---

## EcclesiaFlow Ecosystem

| Module | Port (REST) | Port (gRPC) | Description |
|--------|-------------|-------------|-------------|
| **Members** (this) | 8080 | 9091 | Member management, confirmation, profiles |
| [Auth](https://github.com/GYOM15/ecclesiaflow-auth-module) | 8081 | 9090 | Keycloak integration, password management |
| Email Service | - | 9092 | Transactional emails (confirmation, welcome) |

```
┌──────────────────────────────────────────────────┐
│                  Client (HTTP)                    │
└──────────┬───────────────────────┬───────────────┘
           │ REST :8080            │ REST :8081
           ▼                       ▼
  ┌─────────────────┐     ┌─────────────────┐
  │  Members Module │◄───►│   Auth Module    │
  │   gRPC :9091    │gRPC │   gRPC :9090     │
  └────────┬────────┘     └─────────────────┘
           │ gRPC :9092
           ▼
  ┌─────────────────┐
  │  Email Service   │
  └─────────────────┘
```

---

## Contributing

1. Create a feature branch from `members-module-dev`
2. Follow commit conventions (see below)
3. Ensure `mvn test` passes (521 tests + 90% coverage)
4. Open a Pull Request

### Commit Convention

```
Subject in English, imperative, first letter capitalized (max 50 chars)

Optional body explaining the why (max 72 chars per line)
```

Examples:
- `Add social onboarding endpoint`
- `Fix email uniqueness check in registration`
- `Wire role-to-scope mapping into validation aspect`

---

## License

MIT License — see [LICENSE](LICENSE) for details.
