package ca.sheridancollege.munjalru.beans;

/**
 * Operational status of a dealership.
 *
 * <ul>
 *   <li>{@link #ACTIVE} — normal operation; users can authenticate and act.</li>
 *   <li>{@link #SUSPENDED} — the dealer and all its users are blocked.
 *       Suspended users' existing JWTs remain valid until expiry
 *       (up to 24 h) because the status is embedded as a claim at
 *       login time rather than checked per-request against the DB.</li>
 * </ul>
 */
public enum DealerStatus {
    ACTIVE,
    SUSPENDED
}
