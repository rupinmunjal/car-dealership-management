# ADR 0002: Use JWT for API authentication

- Status: Accepted
- Date: 2026-08-02

## Context

The Angular single-page application consumes a REST API and needs role,
dealership, and employee-permission context on each request. The application is
intended to run as a stateless container and may have multiple backend instances.

## Decision

Successful authentication returns a short-lived, signed JWT. Angular sends it
in the `Authorization: Bearer` header. Spring Security verifies the signature
and expiration, then reloads the user before establishing the request security
context. Authorization rules are enforced on the server regardless of what the
Angular route guards display.

The token contains the role, dealer identifier, dealer status, and employee
permissions for client-side navigation. Passwords and sensitive dealer data are
not included.

## Consequences

- API instances do not require shared HTTP-session state.
- The client can determine the correct dashboard and visible actions without a
  separate session lookup.
- Revocation is less immediate than invalidating a server session. Reloading the
  user on each request prevents deleted or disabled accounts from continuing to
  authenticate, while token expiration bounds remaining claim staleness.
- Token storage and transport require strict XSS controls and HTTPS in deployed
  environments.

## Alternatives considered

- Server-side session cookies. Rejected for this project because they introduce
  shared session storage or instance affinity and couple the REST API to browser
  session semantics.
- Opaque bearer tokens. Rejected because they require a server-side lookup for
  every token and provide no useful role context to the SPA.
