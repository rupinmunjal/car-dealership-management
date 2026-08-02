# Car Dealership Management System

A full-stack web application for managing car dealerships with role-based access
control, built with **Spring Boot 3.5** (Java 21) and **Angular 21** (Angular Material).
Designed as a portfolio/showcase project demonstrating JWT authentication,
authorization scoping, package-based limits, and a polished UI.

---

## 👥 Role Model

| Role | Description | Dashboard |
|---|---|---|
| **SITE_ADMIN** | Platform super-admin. Manages all dealers, packages, and can perform any action. | Dealer list, package CRUD, create dealer |
| **DEALER_ADMIN** | Owns one dealership. Manages cars, employees, and dealer settings. | Their dealership's cars, employee management, settings |
| **DEALER_EMPLOYEE** | Works at one dealership. Access scoped by granted permissions (`CAN_ADD_CAR`, `CAN_EDIT_CAR`, `CAN_DELETE_CAR`). | Permission-scoped: UI hides actions the employee can't perform |

**Login flow:** User logs in once → JWT carries their role → Angular route guard
redirects them to the correct dashboard automatically. No manual role selection.

---

## 📦 Package & Permission System

- **SITE_ADMIN** creates subscription **Packages** (e.g. "Starter" with 5 seats, 20 car listings).
- **SITE_ADMIN** assigns a package to each **Dealer** during registration.
- The package enforces:
  - **Max employee seats** — blocks new hires when limit reached (409 Conflict).
  - **Max car listings** — blocks new cars when limit reached (409 Conflict).
- **Package downgrade:** Existing employees are **not** retroactively deactivated.
  Only new hires are blocked. (See `docs/DESIGN_DECISIONS.md` for rationale.)
- **DEALER_EMPLOYEE** permissions (`CAN_ADD_CAR`, `CAN_EDIT_CAR`, `CAN_DELETE_CAR`)
  are managed by DEALER_ADMIN through the employee management UI.

---

## 🛠️ Technology Stack

### Backend
- **Java 21** with **Spring Boot 3.5.7**
- **Spring Security** — JWT authentication (jjwt 0.12.6)
- **Spring Data JPA** — Hibernate ORM
- **H2** (local dev) / **PostgreSQL** (production)
- **Lombok** — boilerplate reduction
- **SpringDoc OpenAPI** (2.8.9) — Swagger UI at `/swagger-ui.html`
- **Maven** — build & dependency management

### Frontend
- **Angular 21** with TypeScript 5.9
- **Angular Material 21** — tables, forms, dialogs, sidenav, cards, chips, tooltips
- **RxJS** — reactive state management
- **Angular Router** — role-based route guards
- **Vitest** — unit testing

### DevOps
- **Docker** multi-stage build + **Docker Compose** (staging & production profiles)
- **Spring Boot Actuator** — health checks
- **Maven Wrapper** — no Maven install required

---

## 🔧 Quick Start (Local Development)

### Prerequisites
- **Java 21**
- **Node.js 20+** (only for building the Angular frontend)
- No PostgreSQL needed — local dev uses H2 in-memory.

### 1. Clone & set up environment

```bash
cd car-dealership-management

# Copy the env template
cp .env.example .env

# Generate a real JWT secret:
# sed -i "s|change-me-to-a-real-base64-secret|$(openssl rand -base64 64)|" .env

# Load env vars
set -a && source .env && set +a
```

### 2. Start the backend

```bash
./mvnw spring-boot:run
```

The app starts at **http://localhost:8080** with:
- Angular frontend served from embedded static files
- Swagger UI at http://localhost:8080/swagger-ui.html
- H2 console at http://localhost:8080/h2-console

### 3. Login as SITE_ADMIN

On first boot, the `DataInitializer` creates the platform admin account using
the `SITE_ADMIN_EMAIL` and `SITE_ADMIN_PASSWORD` env vars:

- **Email:** the `SITE_ADMIN_EMAIL` value from `.env`
- **Password:** the `SITE_ADMIN_PASSWORD` value from `.env`

After logging in, you'll be routed to the SITE_ADMIN dashboard.

### 4. Build the frontend (only if you modify Angular code)

```bash
cd src/main/webapp
npm install
npm run build    # builds and copies to src/main/resources/static/
```

The frontend dev server (`npm start` on port 4200) proxies API calls to the
backend via `proxy-conf.json`.

---

## 🐳 Docker Deployment

### Staging (port 5000)

```bash
docker compose up --build -d database staging
```

### Production (port 6001)

```bash
docker compose up --build -d database production
```

Both services require these env vars (set in `.env`):
- `JWT_SECRET` — Base64-encoded signing key (required, no default)
- `SITE_ADMIN_EMAIL` / `SITE_ADMIN_PASSWORD` — first-run admin bootstrap
- `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` — database credentials

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test
```

### Test suites

| Suite | Count | Covers |
|---|---|---|
| `PackageLimitEnforcementTest` | 5 | Car listing limits, seat limits, package downgrade, deactivated employees |
| `Phase2ManagementTest` | 14 | Role enforcement (SITE_ADMIN-only endpoints), seat limits, suspension, cross-dealer scoping |
| `DealerScopingTest` | 14 | Cross-dealer isolation, DEALER_EMPLOYEE permissions, SITE_ADMIN access |
| `SecurityTest` | 4 | SQL injection, XSS resistance, auth checks, invalid JWT handling |
| `FunctionalTest` | 6 | End-to-end API flows |
| `CarRestControllerTest` | 3 | Car endpoints with mocked service |
| `SmokeTest` | 3 | Application context, health checks |
| `PerformanceTest` | 2 | Response time assertions |
| `JwtServiceTest` | 3 | Token generation, validation, expiry |
| `AuthenticationServiceTest` | 2 | Login/register flows |
| `A2ApplicationTests` | 1 | Spring context loads |

---

## 🔒 Environment Variables

| Variable | Required | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Yes | `local` (H2 + Swagger) or `prod` (PostgreSQL) |
| `JWT_SECRET` | **Yes** | Base64 secret for JWT signing. Generate: `openssl rand -base64 64` |
| `JWT_EXPIRATION_MS` | No | Token lifetime in ms (default: 86400000 = 24 h) |
| `SITE_ADMIN_EMAIL` | **Yes** | First-run admin account email |
| `SITE_ADMIN_PASSWORD` | **Yes** | First-run admin account password |
| `SPRING_DATASOURCE_URL` | External prod only | PostgreSQL JDBC URL; Compose sets this internally |
| `SPRING_DATASOURCE_USERNAME` | External prod only | Database username; Compose derives this from `POSTGRES_USER` |
| `SPRING_DATASOURCE_PASSWORD` | External prod only | Database password; Compose derives this from `POSTGRES_PASSWORD` |
| `POSTGRES_USER` | Docker only | Postgres container user |
| `POSTGRES_PASSWORD` | Docker only | Postgres container password |
| `POSTGRES_DB` | Docker only | Postgres database name |

---

## 🧭 How to Test Each Role Locally

### SITE_ADMIN
1. Start the app → login with the bootstrapped admin credentials.
2. You land on the SITE_ADMIN dashboard.
3. **Create a Package** (e.g. "Starter" with 5 seats, 20 cars).
4. **Register a Dealer** — this creates both the dealer and its first
   DEALER_ADMIN account.

### DEALER_ADMIN
1. Use the credentials from dealer registration (the `adminEmail`/`adminPassword`
   you provided).
2. You land on the DEALER_ADMIN dashboard showing that dealer's cars, employee
   management, and settings.
3. **Create employees** (up to the package seat limit).
4. **Add cars** (up to the package listing limit).

### DEALER_EMPLOYEE
1. Log in with an employee account created by a DEALER_ADMIN.
2. You land on the DEALER_EMPLOYEE dashboard.
3. UI buttons are shown/hidden based on your permissions — e.g. "Add Car" is
   hidden if you lack `CAN_ADD_CAR`.

---

## 📖 API Documentation

- **Swagger UI:** http://localhost:8080/swagger-ui.html (local profile only)
- **Raw OpenAPI:** http://localhost:8080/v3/api-docs

Endpoints are grouped into: **Auth**, **Cars**, **Dealers**, **Packages**, **Employees**.

---

## 📁 Project Structure

```
car-dealership-management/
├── src/main/java/.../
│   ├── beans/          # JPA entities: Car, Dealer, User, Package, Permission, Role
│   ├── config/         # Security, JWT filter, DataInitializer, OpenAPI config
│   ├── controllers/    # REST controllers (Car, Dealer, Employee, Package, Auth)
│   ├── dto/            # Request/Response DTOs with validation
│   ├── exception/      # GlobalExceptionHandler + ApiError
│   ├── mapper/         # Entity ↔ DTO mapping
│   ├── repositories/   # Spring Data JPA repositories
│   └── services/       # Business logic
├── src/main/resources/
│   ├── application.yml           # Base config
│   ├── application-local.yml     # H2, verbose logs, Swagger on
│   ├── application-prod.yml      # PostgreSQL, minimal logs, Swagger off
│   └── static/                   # Built Angular frontend
├── src/main/webapp/              # Angular source
│   └── src/app/
│       ├── pages/                # Login, dashboards (3 roles), forms, tables
│       ├── guards/               # Role-based route guards
│       ├── interceptors/         # JWT injection
│       ├── services/             # Auth, Car, Dealer, Health
│       └── components/           # Shared UI primitives
├── src/test/                     # 57 tests (11 suites)
├── compose.yaml                  # Docker Compose (staging + production + Postgres)
├── Dockerfile                    # Multi-stage build (Maven → JRE)
└── .env.example                  # Environment variable template
```

---

## 📄 License

This project is part of academic coursework at Sheridan College.

## 👥 Authors

- **Rupin Munjal**
- **Amninder Kaur**
