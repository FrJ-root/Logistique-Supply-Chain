# Logistics Management API

## 📦 Overview

This project is a **modular REST API for logistics management**, designed to handle products, multi-warehouse inventory, suppliers, purchase orders, customer orders, shipments, and full stock traceability.

The project is also a **pedagogical reference** demonstrating:
- clean layered architecture (Controller / Service / Repository / DTO),
- advanced logistics business rules,
- modern security approaches (Basic Auth, JWT, Keycloak),
- CI/CD pipelines with quality gates,
- observability and auditability.

---

## 🎯 Project Goals

### Functional Goals
- Full traceability of logistics operations
- Zero negative stock guarantee
- Automated reservation, shipping, and replenishment rules
- Clear separation of responsibilities by business role

### Technical Goals
- Clean, testable Spring Boot architecture
- Centralized security management
- Automated quality control via CI/CD
- Measurable and enforceable code quality

---

## 🏗️ Technical Stack

- **Backend**: Spring Boot, Spring Web
- **Persistence**: Spring Data JPA, Hibernate
- **Mapping**: MapStruct
- **Validation**: Jakarta Validation
- **Documentation**: Swagger / OpenAPI
- **Testing**: JUnit 5, Mockito
- **Security**:
    - Spring Security (Basic Auth, JWT)
    - Keycloak (OIDC, OAuth2)
- **CI/CD**: Jenkins, Maven Wrapper
- **Code Quality**: JaCoCo, SonarQube
- **Observability (optional)**: Elasticsearch, Kibana
- **Containerization (optional)**: Docker

---

## 🌿 Git Branch Strategy

| Branch | Purpose |
|------|--------|
| `main` | Core logistics API (business logic, no advanced security) |
| `basic-auth` | Spring Security Basic Authentication (POC) |
| `jwt-sec` | Stateless JWT Access & Refresh Token security |
| `keycloak` | Full Keycloak integration (OIDC / OAuth2) |

---

## 🔐 Security Architecture

### 1️⃣ Basic Authentication (branch: `basic-auth`)

**Objective**: Understand Spring Security fundamentals.

- HTTP Basic Auth
- In-memory users
- Stateless API
- HTTPS required

**Roles**
- `ADMIN`
- `WAREHOUSE_MANAGER`
- `CLIENT`

**Protected Endpoints**
- `/api/products/**`
- `/api/inventory/**`
- `/api/orders/**`
- `/api/shipments/**`
- `/api/admin/**`

**HTTP Status Handling**
- `200` OK
- `401` Unauthorized
- `403` Forbidden

---

### 2️⃣ JWT Security (branch: `jwt-sec`)

**Objective**: Stateless, production-ready API security.

#### Access Token
- JWT format
- Contains user ID and roles
- Short lifespan (≈ 15 minutes)
- Signed and validated on each request

#### Refresh Token
- Long lifespan (≈ 7 days)
- Stored securely
- Rotation enforced
- Revoked on logout or account deactivation

#### Error Handling
- `401`: invalid or expired token
- `403`: insufficient role

---

### 3️⃣ Keycloak Integration (branch: `keycloak`)

Keycloak is used as the **central Identity & Access Management system**.

#### Realm
- **Name**: `logistics-realm`
- No application configuration in `master` realm

#### Responsibilities
- User authentication
- Role management
- Token issuance
- Session & logout handling
- Audit logging

#### Roles
- `ADMIN`
- `WAREHOUSE_MANAGER`
- `CLIENT`

#### Groups (recommended)
| Group | Role |
|-----|-----|
| `admins` | ADMIN |
| `warehouse-managers` | WAREHOUSE_MANAGER |
| `clients` | CLIENT |

#### OIDC Clients

**Frontend Client**
- Type: Public
- Flow: Authorization Code
- Tokens: Access + Refresh

**API Client**
- Type: Bearer-only or Confidential
- Validates tokens issued by Keycloak

#### Token Rules
- JWT signed by Keycloak
- Issuer validation (realm)
- Expiration enforced
- Revoked on logout or incident

---

## 👥 Business Roles

| Role | Responsibilities |
|----|------------------|
| **ADMIN** | Users, products, warehouses, suppliers, purchase orders |
| **WAREHOUSE_MANAGER** | Stock, movements, reservations, shipments |
| **CLIENT** | Orders creation, tracking, consultation |

---

## 📚 Functional Scope

### Products & Inventory
- SKU-based product management
- Multi-warehouse inventory
- Stock availability = `qtyOnHand - qtyReserved`
- Movements: `INBOUND`, `OUTBOUND`, `ADJUSTMENT`

### Orders & Shipments
- Sales Orders lifecycle:
  `CREATED → RESERVED → SHIPPED → DELIVERED → CANCELED`
- Mandatory reservation before shipping
- Automatic backorders
- Cut-off time (15h)
- Shipment capacity per slot

### Suppliers & Purchase Orders
- Partial or full reception
- Automatic stock update
- Full traceability

---

## 🧠 Advanced Business Rules

- ❌ No negative stock (strict)
- 🔒 Mandatory reservation before shipping
- 🏬 Multi-warehouse allocation
- 🔁 Automatic backorders
- ⏱️ Reservation TTL (24h)
- 🚚 Shipment slot capacity control
- 📅 Cut-off logistics time enforcement

---

## 🧩 Architecture


### Layers
- **Controller**: REST endpoints & validation
- **Service**: Business logic
- **Repository**: Persistence
- **DTO**: API contracts (no business logic)
- **Mapper**: Entity ↔ DTO conversion

---

## ⚠️ Validation & Exception Handling

### Validation
- `@NotNull`, `@NotBlank`, `@Min`, `@Email`, etc.
- Applied on DTOs
- Clear JSON error responses

### Global Exception Handling
Handled via `@ControllerAdvice`

| Exception | HTTP |
|--------|------|
| ResourceNotFoundException | 404 |
| BusinessException | 400 |
| ValidationException | 400 |
| StockUnavailableException | 409 |
| GenericException | 500 |

---

## 🧪 Testing Strategy

### Unit Tests
- Stock constraints
- Reservation & release
- Status transitions
- Backorders
- Cut-off logic
- Mapper validation
- Exception scenarios

Run tests:
```bash
mvn test
```

## 🔁 CI/CD Pipeline
### 🛠️ Tools

- Jenkins
- Maven Wrapper

- JaCoCo

- SonarQube

- Docker (optional)

### ⚙️ Pipeline Steps

- Build on push / pull request

- Run unit tests

- Generate coverage report

- SonarQube analysis

- Quality Gate enforcement

- Package Maven artifacts

### 📏 Quality Thresholds

- Coverage ≥ 80 %

- New code coverage ≥ 90 %

- Duplications ≤ 5 %

- Bugs & vulnerabilities: 0 accepted

- Maintainability: A

DTOs, Mappers, and generated classes are excluded from coverage calculation.

## 📊 Observability (Optional)

- Elasticsearch for log indexing

- Kibana for visualization

### Logs

- application

- security

- business events

No secrets, tokens, or passwords are ever logged.

## 🎓 Pedagogical Context
### Logistics API

- Start: 29/12/2025

- Deadline: 02/01/2026

- Work mode: Pair programming

### CI/CD & Quality

- Start: 10/11/2025

- Deadline: 14/11/2025

- Work mode: Individual

### Presentation (30 minutes)

- Demo (10 min)

- Code & architecture explanation (10 min)

- Scenario / use case (5 min)

- Q/A (5 min)

## ✅ Deliverables

- Functional logistics API

- Security implementations (Basic Auth, JWT, Keycloak)

- CI/CD pipeline with enforced quality gates

- Test and coverage reports

- Technical documentation

- Final quality report

## 🚀 Conclusion

This project is both a realistic logistics backend and a complete learning path, covering:

- domain-driven business logic

- modern Spring Security

- IAM with Keycloak

- CI/CD and code quality

- observability and auditability