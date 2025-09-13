# EcclesiaFlow Members Module

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-9.0.0-blue.svg)](https://dev.mysql.com/downloads/mysql/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.1.1-green.svg)](https://swagger.io/specification/)

> **Member management module for the EcclesiaFlow platform**

Microservice dedicated to comprehensive church member management: registration, email confirmation, profile management and integration with the authentication module. Designed following Clean Architecture principles with clear separation of responsibilities.

---

## Table of Contents

- [Overview](#overview)
- [Module Features](#module-features)
- [Project Structure](#project-structure)
- [Multi-Tenant Architecture](#multi-tenant-architecture)
- [Clean Architecture - 4 Layers](#clean-architecture---4-layers)
- [EcclesiaFlow Ecosystem](#ecclesiaflow-ecosystem)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [API Examples (cURL + jq)](#api-examples-curl--jq)
- [Configuration](#configuration)
- [Docker Deployment](#docker-deployment)
- [Testing and Quality](#testing-and-quality)
- [Contributing](#contributing)
- [License](#license)

---

## 🚀 Overview

The **EcclesiaFlow Members Module** is a specialized microservice for comprehensive church member management. It is part of the EcclesiaFlow ecosystem, a multi-tenant SaaS platform where each church operates as an independent tenant.

### 🎯 **Module Responsibilities**

- **Member Management**: Registration, profiles, information updates
- **Confirmation Process**: Email validation with temporary codes  
- **Auth Integration**: Communication with authentication module for tokens
- **Notifications**: Automatic confirmation email sending
- **Clean Architecture**: Clear separation of layers (Web, Business, IO, Shared)

```mermaid
sequenceDiagram
    participant Client
    participant Members as Members Module (8080)
    participant Auth as Auth Module (8081)
    participant Email as Email Service

    Note over Client,Email: Registration → confirmation → password → final token

    Client->>Members: POST /ecclesiaflow/members
    Members->>Email: Send confirmation code
    Members-->>Client: 201 Created

    Client->>Members: PATCH /ecclesiaflow/members/:memberId/confirmation { code }
    Members->>Auth: Request temporary token
    Auth-->>Members: Temporary token
    Members-->>Client: 200 OK + temporaryToken + passWordEnpoint
    
    Note over Client: Store token securely (sessionStorage)<br/>Redirect to clean URL (no token)

    Client->>Auth: POST /password
    Note right of Client: Authorization: Bearer temporaryToken<br/>Body: { newPassword }
    Auth-->>Client: 200 OK { accessToken, refreshToken }
```

---

## ✨ Module Features

* 👥 **Member Management** – Complete member profile CRUD with business validation
* ✉️ **Email Confirmation** – Secure process with temporary codes (6 digits) and safe token handling
* 🔗 **Auth Module Integration** – WebClient communication for temporary tokens with secure URL generation
* 📧 **Email Notifications** – Integrated SMTP service with customizable templates
* 🏗️ **Clean Architecture** – 4 layers: Web, Business, IO, Shared
* 📚 **API-First Design** – Complete OpenAPI documentation with detailed schemas
* 🧪 **Comprehensive Testing** – JaCoCo coverage with unit and integration tests
* 🔄 **AOP Logging** – Business and technical aspects for monitoring
* 🛡️ **Error Handling** – GlobalExceptionHandler with standardized responses

---

## 📁 Project Structure

```
ecclesiaflow-members-module/
├── src/
│   ├── main/
│   │   ├── java/com/ecclesiaflow/
│   │   │   ├── MembersModuleApplication.java
│   │   │   ├── application/                 # Application Layer
│   │   │   │   ├── config/
│   │   │   │   └── logging/
│   │   │   ├── business/                    # Business Layer
│   │   │   │   ├── domain/
│   │   │   │   │   ├── communication/
│   │   │   │   │   ├── confirmation/
│   │   │   │   │   ├── member/
│   │   │   │   │   └── token/
│   │   │   │   ├── exceptions/
│   │   │   │   └── services/
│   │   │   ├── io/                          # IO Layer
│   │   │   │   ├── communication/
│   │   │   │   ├── exception/
│   │   │   │   ├── notification/
│   │   │   │   └── persistence/
│   │   │   └── web/                         # Web Layer
│   │   │       ├── client/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       ├── mappers/
│   │   │       ├── payloads/
│   │   │       └── security/
│   │   └── resources/
│   │       ├── api/
│   │       │   └── members.yaml             # API-First OpenAPI
│   │       └── application.properties.example
│   └── test/java/com/ecclesiaflow/          # Tests
│       ├── application/
│       ├── business/
│       ├── io/
│       └── web/
├── target/
├── pom.xml
├── README.md
├── LICENSE
├── .env.example
├── .gitignore
└── mvnw, mvnw.cmd                          # Maven Wrapper
```

---

## 🏛️ Multi-Tenant Architecture

### Target Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    SUPER ADMIN                              │
├─────────────────────────────────────────────────────────────┤
│  TENANT 1 (Church A)    │  TENANT 2 (Church B)    │ ...    │
│  ┌─────────────────────┐ │ ┌─────────────────────┐  │        │
│  │ Pastor (Admin)      │ │ │ Pastor (Admin)      │  │        │
│  │ ├─ Member 1         │ │ │ ├─ Member 1         │  │        │
│  │ ├─ Member 2         │ │ │ ├─ Member 2         │  │        │
│  │ └─ ...              │ │ │ └─ ...              │  │        │
│  │ └─────────────────────┘ │ └─────────────────────┘  │        │
└─────────────────────────────────────────────────────────────┘
```

### Roles and Responsibilities

- **Super Admin**: Global management of all tenants (churches)
- **Pastor (Tenant Admin)**: Church administration, member management
- **Members**: Church members with profiles and participation

---

## 🏗️ Clean Architecture - 4 Layers

The module follows **Clean Architecture** principles with clear separation of responsibilities:

### 📱 **Web Layer** (`com.ecclesiaflow.web`)
- **Controllers**: `MembersController`, `MembersConfirmationController`
- **DTOs**: `SignUpResponse`, `ConfirmationResponse`, `MemberResponse`
- **Payloads**: `SignUpRequestPayload`, `ConfirmationRequestPayload`, `UpdateMemberRequestPayload`
- **Mappers**: Conversion between DTOs/Payloads and domain objects
- **Client**: `AuthClient` for inter-module communication
- **Security**: Authentication and authorization components
- **Exceptions**: `GlobalExceptionHandler`, custom web exceptions

### 🏢 **Business Layer** (`com.ecclesiaflow.business`)
- **Services**: `MemberService`, `MemberConfirmationService` and implementations
- **Domain**: 
  - **Member**: `Member`, `MemberRepository`, `MembershipRegistration`, `MembershipUpdate`
  - **Confirmation**: `MemberConfirmation`, `MembershipConfirmationResult`
  - **Communication**: Email and notification contracts
  - **Token**: Temporary token management
- **Exceptions**: `MemberNotFoundException`, `EmailSendingException`

### 💾 **IO Layer** (`com.ecclesiaflow.io`)
- **Persistence**: JPA entities, Spring Data repositories, and entity mappers
- **Communication**: 
  - **Email**: `EmailServiceImpl` for SMTP communication
  - **SMS**: SMS service implementation (future implementation)
- **Notification**: 
  - **Email**: `EmailConfirmationNotifier` for email notifications
  - **SMS**: SMS notification infrastructure (future implementation)
- **Exceptions**: IO-specific exceptions and error handling

### 🔧 **Application Layer** (`com.ecclesiaflow.application`)
- **Config**: Application-wide configuration classes
- **Logging**: AOP aspects for business and technical logging
  - `LoggingAspect`: Technical logging (performance, exceptions)
  - `BusinessOperationLoggingAspect`: Business operations logging

## 📦 EcclesiaFlow Ecosystem

* **Members Module** (This module)
  **Port**: 8080
  **Role**: Member management

* **Authentication Module**
  🔗 [GitHub Repo](https://github.com/GYOM15/ecclesiaflow-auth-module)
  **Port**: 8081
  **Role**: JWT authentication and password management

---

## 🛠 Technology Stack

* **Backend**: Java 21, Spring Boot 3.5.5
* **Database**: MySQL 9.0.0 with Spring Data JPA
* **Communication**: Spring WebFlux, WebClient (Auth Module)
* **Email**: Spring Boot Mail with Gmail SMTP
* **Documentation**: OpenAPI 3.1.1, SpringDoc, Swagger UI
* **Build**: Maven 3.14.0 with optimized plugins
* **Testing**: JUnit 5, Mockito 5.14.2, JaCoCo 0.8.11
* **Logging**: AOP with AspectJ, SLF4J
* **Architecture**: Clean Architecture, Microservices, SOLID

---

## ⚡ Quick Start

### 1. Prerequisites

* **Java 21+** (OpenJDK or Oracle JDK)
* **Maven 3.8+** for dependency management
* **MySQL 9.0+** (or MySQL 8.0+ compatible)
* **IDE** (IntelliJ IDEA recommended with Lombok support)
* **Auth Module** running on port 8081

### 2. Clone Project

```bash
git clone https://github.com/GYOM15/ecclesiaflow-members-module.git
cd ecclesiaflow-members-module
```

### 3. Database Configuration

```sql
-- Members Module Database
CREATE DATABASE ecclesiaflow_members;
CREATE USER 'ecclesiaflow'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON ecclesiaflow_members.* TO 'ecclesiaflow'@'localhost';
FLUSH PRIVILEGES;
```

### 4. Application Configuration

```bash
# Copy example configuration file
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit with your actual values
nano src/main/resources/application.properties
```

**Required variables to configure:**
```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/ecclesiaflow_members
spring.datasource.username=ecclesiaflow
spring.datasource.password=your_secure_password

# Email SMTP (Gmail)
spring.mail.username=your-email@gmail.com
spring.mail.password=your_gmail_app_password

# Auth Module
ecclesiaflow.auth.module.base-url=http://localhost:8081
```

### 5. Start Module

```bash
# Compile and test
mvn clean compile test

# Start in development mode
mvn spring-boot:run

# Or with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 6. Verify Startup

```bash
# Health check
curl http://localhost:8080/actuator/health

# API documentation
open http://localhost:8080/swagger-ui.html

# Test endpoint
curl http://localhost:8080/ecclesiaflow/hello
```

---

## 🔑 API Examples (cURL + jq)

### 📝 **Member Registration**

```bash
# 1. Register new member
curl -X POST "http://localhost:8080/ecclesiaflow/members" \
  -H "Content-Type: application/vnd.ecclesiaflow.members.v1+json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe", 
    "email": "john.doe@example.com",
    "address": "123 Peace Street, Paris"
  }' | jq .
```

### ✅ **Account Confirmation**

```bash
# 2. Confirmation with code received by email
curl -X PATCH "http://localhost:8080/ecclesiaflow/members/550e8400-e29b-41d4-a716-446655440000/confirmation" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "123456"
  }' | jq .

 Response: 200 OK
{
  "message": "Compte confirmé avec succès.",
  "temporaryToken": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresIn": 3600,
  "passwordEndpoint": "http://localhost:8081/ecclesiaflow/auth/password"
}
```

### 🔐 **Exemple of Password Setup (Direct API Flow)**

```bash
# 3. Set password using temporary token (secure flow)
curl -X POST "http://localhost:8081/ecclesiaflow/auth/password" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "password": "MonMotDePasse123!"
  }' | jq .

 Response: 200 OK
{
  "message": "Mot de passe défini avec succès",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 🔒 **Secure Token Handling Example - JavaScript (Direct Flow)**

1. **Token in JSON Response**: The temporary token is returned in the response body, not in the URL
2. **Direct API Call**: No client-side storage needed - token used immediately
3. **Immediate Usage**: Token exists only for the duration of the password setup request

**Client-side implementation (Direct Flow):**
```javascript
// Direct flow without client-side storage
const confirmationResponse = await fetch('/ecclesiaflow/members/123/confirmation', {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code: '123456' })
});

const { temporaryToken, redirectUrl } = await confirmationResponse.json();

// Use token immediately for password setup
const passwordResponse = await fetch(redirectUrl, {
    method: 'POST',
    headers: {
        'Authorization': `Bearer ${temporaryToken}`,
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({ 
        email: 'user@example.com',
        password: 'nouveauMotDePasse123' 
    })
});

const result = await passwordResponse.json();
console.log('Password set successfully:', result);
```

### 🔄 **Resend Confirmation Code**

```bash
# 3. Resend new confirmation code
curl -X POST "http://localhost:8080/ecclesiaflow/members/550e8400-e29b-41d4-a716-446655440000/confirmation-code" \
  -H "Content-Type: application/vnd.ecclesiaflow.members.v1+json" | jq .
```

---

## ⚙️ Configuration

### 🗃️ **Database Variables**

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/ecclesiaflow_members
spring.datasource.username=ecclesiaflow
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 📧 **SMTP Email Configuration**

```properties
# Gmail SMTP (recommended)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your_gmail_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# EcclesiaFlow Configuration
ecclesiaflow.mail.from=your-email@gmail.com
ecclesiaflow.app.name=EcclesiaFlow
```

### 🔗 **Auth Module Integration**

```properties
# Authentication module URL
ecclesiaflow.auth.module.base-url=http://localhost:8081
```

### 📚 **OpenAPI Documentation**

```properties
# SpringDoc OpenAPI
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.paths-to-match=/ecclesiaflow/**
springdoc.packages-to-scan=com.ecclesiaflow.web.controller
```

### 🔧 **Environment Variables (.env)**

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecclesiaflow_members
DB_USERNAME=ecclesiaflow
DB_PASSWORD=your_secure_password

# Email
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
MAIL_FROM=your-email@gmail.com

# External services
AUTH_MODULE_BASE_URL=http://localhost:8081

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

---

## 🐳 Docker Deployment

### **Dockerfile**

```dockerfile
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Start application
CMD ["java", "-jar", "target/ecclesiaflow-members-module-1.0.0-SNAPSHOT.jar"]
```

### **Docker Compose**

```yaml
version: '3.8'
services:
  members-module:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=mysql
      - DB_NAME=ecclesiaflow_members
      - DB_USERNAME=ecclesiaflow
      - DB_PASSWORD=${DB_PASSWORD}
      - AUTH_MODULE_BASE_URL=http://auth-module:8081
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - ecclesiaflow-network

  mysql:
    image: mysql:9.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ecclesiaflow_members
      MYSQL_USER: ecclesiaflow
      MYSQL_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 20s
      retries: 10
    networks:
      - ecclesiaflow-network

volumes:
  mysql_data:

networks:
  ecclesiaflow-network:
    external: true
```

---

## 🧪 Testing and Quality

### **Running Tests**

```bash
# Unit tests only
mvn test

# Tests with JaCoCo coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Integration tests
mvn verify -P integration-tests
```

### **Test Structure**

```
src/test/java/com/ecclesiaflow/
├── application/
│   ├── config/                 # Application config tests
│   └── logging/
│       └── aspect/             # AOP aspect tests (application logging)
├── business/
│   ├── services/impl/          # Business service tests
│   └── aspect/                 # AOP aspect tests
├── web/
│   ├── controller/             # REST controller tests
│   └── exception/              # Error handling tests
└── io/
    └── persistence/            # Repository tests
```

### **Quality Metrics**

- **JaCoCo Coverage**: Minimum 80% per package
- **Unit Tests**: Mockito with LENIENT strictness
- **Integration Tests**: TestContainers for MySQL
- **Architecture**: ArchUnit for layer validation

---

## 🤝 Contributing

### **Development Standards**

- **Clean Architecture**: Respect the 4-layer separation
- **SOLID Principles**: Each class has a single responsibility
- **Documentation**: Complete Javadoc for all public classes
- **Testing**: Minimum 80% coverage with unit and integration tests

### **Git Workflow**

```bash
# 1. Start from development branch
git checkout members-module-dev

# 2. Create branch for new feature
git checkout -b new-feature

# 3. Develop with atomic commits
git commit -m "feat(members): Add email validation"

# 4. Tests and quality
mvn clean test jacoco:report

# 5. Push and Pull Request to members-module-dev
git push origin new-feature
```

### **Commit Convention**

**Format with type:**
```
Type(scope): description (≤ 50 characters, first letter capitalized)

Message body (≤ 72 characters per line)

Types: Feat, Fix, Docs, Style, Refactor, Test, Chore
Scopes: members, confirmation, email, persistence, web

NB: scope is optionnal
```

**Format without type:**
```
Add new feature (≤ 50 characters, first letter capitalized)

Detailed message body if necessary
(≤ 72 characters per line)
```

**Examples:**
- `Feat(members): add email validation service`
- `Fix(confirmation): resolve code expiration issue`
- `Feat: resolve code expiration issue`
- `Add comprehensive member profile validation`
- `Update OpenAPI documentation for new endpoints`

---

## 📄 License

MIT – see [LICENSE](LICENSE)

---

**Developed with ❤️ for church communities worldwide**
