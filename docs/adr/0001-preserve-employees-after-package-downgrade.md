# ADR 0001: Preserve employees after a package downgrade

- Status: Accepted
- Date: 2026-08-02

## Context

A dealership package limits employee seats and vehicle listings. A dealer can
move to a package whose employee limit is lower than its current active employee
count. Automatically choosing accounts to deactivate would interrupt work,
could lock out operationally important users, and would make a billing change
perform an unrelated identity-management action.

## Decision

A package downgrade does not deactivate existing employees. The new limit is
enforced when the dealer next attempts to hire an employee. New hires remain
blocked until the active employee count falls below the package limit or the
dealer moves to a larger package.

Dealer administrators retain explicit control over which employee accounts are
active. The dashboard may show that current usage exceeds the assigned limit.

## Consequences

- A downgrade is predictable and does not remove access without an administrator
  selecting the affected account.
- Package enforcement is eventual for seat usage rather than retroactive.
- A dealer can temporarily remain above the package limit.
- The hire path must count active, non-deleted employees transactionally before
  creating another account.

## Alternatives considered

- Deactivate the newest employees automatically. Rejected because creation time
  is not a reliable measure of operational importance.
- Reject all downgrades that exceed current usage. Rejected because it makes
  package management unnecessarily rigid and complicates support workflows.
