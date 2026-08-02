# ADR 0003: Cache dashboard summaries, not authentication tokens

- Status: Accepted
- Date: 2026-08-02

## Context

Dealer dashboards repeatedly aggregate car counts, active employee counts, and
package limits. These values are read frequently, tolerate brief staleness, and
change through a small set of known mutations. Authentication and authorization
state has different correctness and security requirements.

## Decision

Redis caches dealer dashboard summaries by dealer identifier for five minutes.
Car, employee, dealer, and package mutations evict the affected summary. The
database remains the system of record.

JWTs and authenticated-user records are not cached in Redis. Spring Security
verifies the signed token and loads current account state for each authenticated
request.

## Consequences

- Repeated dashboard visits avoid redundant aggregate database work.
- A missed eviction can expose stale summary values for at most five minutes,
  without changing the underlying records or authorization decisions.
- Authentication remains correct when an account is disabled, deleted, or has
  permissions changed.
- Redis is required by the production profile but not by H2-based local tests.

## Alternatives considered

- Cache every repository response. Rejected because invalidation would be broad,
  complex, and error-prone for limited portfolio-scale workloads.
- Cache token validation or user authorization state. Rejected because stale
  entries could preserve access after account or permission changes.
- Do not cache. Rejected because dashboard aggregation is a clear, bounded use
  case that demonstrates measurable cache behavior and explicit eviction.
