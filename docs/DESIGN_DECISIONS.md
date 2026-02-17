# Design Decisions — Car Dealership Management System

A summary of the key architectural tradeoffs made across this project, written
so you can reference them before a technical interview.

---

## 1. JWT-embedded dealer status vs. DB check per request

**Decision:** Dealer suspension status is embedded in the JWT at login time and
extracted by `JwtAuthenticationFilter` on every request — we do **not** query
the database for dealer status on each request.

**Why:**
- Avoids an extra database round-trip for every authenticated request.
- The JWT is short-lived (default 24 h), so stale suspension data resolves
  quickly.
- Suspension checks happen in controllers via
  `currentUser.getDealerStatus() == DealerStatus.SUSPENDED`, keeping the
  authorization logic readable.

**Tradeoff:** A dealer suspended immediately after login can still act until
their JWT expires. For a real production system with stricter requirements,
you'd add a short-lived cache (e.g. Redis with 60 s TTL) checked on each
request, or use refresh-token revocation. For this portfolio project, the JWT
approach is a pragmatic balance of performance and security.

---

## 2. Employee deactivation vs. hard delete

**Decision:** Employee accounts are soft-deactivated (`active = false`) rather
than hard-deleted from the database. Deactivated employees are excluded from
`EmployeeService.listEmployees()` listings but their existing JWTs remain
valid until expiry.

**Why:**
- Preserves audit trail — you can still see who performed past actions.
- Allows re-activation without re-creating accounts.
- Same JWT-expiry tradeoff as dealer suspension (see #1). Consistent mental
  model across the codebase.
- `User.isEnabled()` returns `active`, which Spring Security would normally
  check in `DaoAuthenticationProvider`, but since we use JWT with a custom
  filter, the check is manual in listing queries.

**Tradeoff:** A deactivated employee can still use a previously-issued JWT
until it expires. This is intentional and documented — the same pattern used
for dealer suspension keeps the codebase consistent.

---

## 3. Package downgrade behaviour

**Decision:** When a dealer is downgraded to a package with fewer seats than
their current active employee count, existing employees are **not**
retroactively deactivated. Only new hires are blocked until the employee count
drops below the new limit.

**Why:**
- Avoids surprising business disruption — an admin changing a package
  shouldn't lock existing employees out of their accounts.
- The admin can manually deactivate employees to bring the count down, then
  the limit enforces itself for new hires.

**Tradeoff:** The dealer temporarily has more active employees than the
package allows. If strict enforcement is needed, the system could be changed
to auto-deactivate the most recently added employees. The current approach
prioritizes stability over strict enforcement.

---

## 4. Angular Material vs. PrimeNG vs. custom CSS

**Decision:** Chose Angular Material for the UI component library.

**Why:**
- Official Google library with guaranteed Angular 21 compatibility.
- Material Design provides a polished, professional look out of the box
  (important for portfolio screenshots).
- Comprehensive component set: tables, forms, dialogs, cards, sidenav, chips,
  tooltips — all needed for this project.
- Strong theming system (indigo-pink theme) with consistent typography and
  spacing.
- Lightweight compared to PrimeNG for the component count we need.

**Tradeoff:** Angular Material tables require a `MatTableDataSource` data
source pattern that feels verbose for simple lists. PrimeNG offers a more
template-driven approach. We accepted the Material verbosity for its
ecosystem stability and professional appearance.

---

## 5. H2 for local dev, PostgreSQL for prod

**Decision:** The `local` Spring profile uses H2 in-memory database;
the `prod` profile uses PostgreSQL.

**Why:**
- H2 starts instantly with zero configuration — no Docker, no Postgres install
  needed for local development.
- `create-drop` DDL in local means a clean database on every restart,
  avoiding test pollution.
- The `DataInitializer` bootstraps a SITE_ADMIN account on first run, so
  local dev is ready to use immediately.
- `prod` profile uses `ddl-auto: update` for safe schema evolution.

**Tradeoff:** H2 and PostgreSQL have subtle SQL dialect differences
(e.g. H2's `DB_CLOSE_DELAY` parameter, identity column behaviour). The
project uses JPA/Hibernate to abstract these, but complex native queries
would need testing against both databases.

---

## 6. Single JAR deployment with embedded Angular

**Decision:** The Angular frontend is built into `src/main/resources/static/`
and served by Spring Boot's embedded Tomcat. There is no separate Node.js
server in production.

**Why:**
- Single deployable artifact — one JAR, one `docker compose` service.
- No CORS issues in production (same origin).
- The Angular dev server (`ng serve` on port 4200) proxies API calls to
  Spring Boot on 8080 during development, keeping the DX smooth.
- Simpler Docker setup — single `Dockerfile` with multi-stage build.

**Tradeoff:** The frontend and backend are tightly coupled in the build
pipeline. For a larger team with separate frontend/backend developers, a
decoupled deployment (separate containers with an API gateway) would be
preferable.

---

## 7. `@PreAuthorize` on controllers vs. service-layer security

**Decision:** Role and permission checks use `@PreAuthorize` annotations on
controller methods, with additional programmatic checks in the controller
body for dealer-scoping and suspension.

**Why:**
- `@PreAuthorize` provides declarative, readable security rules at the
  API entry point.
- Programmatic checks (`currentUser.getDealer().getId()`) handle the
  dynamic dealer-scoping that can't be expressed in SpEL alone.
- Service layer remains security-agnostic, making services testable
  without Spring Security context.

**Tradeoff:** Some authorization logic is duplicated across controllers
(e.g. the null check + role check + dealer scope check pattern). A custom
`@PreAuthorize` expression handler or an aspect could DRY this up.

---

## 8. No refresh tokens

**Decision:** The JWT implementation uses a single access token with a
configurable expiration (default 24 h). There is no refresh token mechanism.

**Why:**
- Simplifies the auth flow significantly — login returns one token, client
  stores it, includes it in `Authorization: Bearer` header.
- For a portfolio project, the added complexity of refresh token rotation,
  token blacklisting, and silent renewal isn't justified.
- The 24-hour default window is generous for demo/testing purposes.

**Tradeoff:** In production, a long-lived access token without refresh means
a compromised token grants access for up to 24 hours. A production system
would add refresh tokens, shorter access token lifetimes (15 min), and
a token blacklist on logout.

---

## Key Takeaways for Interview Discussion

- **Security-first mindset:** JWT secret is required via env var with no
  hardcoded fallback. SQL injection and XSS vectors are tested.
- **Practical tradeoffs:** Dealer suspension and employee deactivation share
  a consistent JWT-expiry pattern — understand the tradeoff and can explain
  how you'd tighten it for production.
- **Developer experience matters:** H2 + `DataInitializer` bootstrap means
  the project starts in one command with `./mvnw spring-boot:run`.
- **Test coverage:** 54 integration tests cover authorization, scoping,
  package limits, suspension, and deactivation flows.
- **Config separation:** `application-local.yml` vs `application-prod.yml`
  with Swagger disabled in production, different CORS origins, and different
  logging levels.
