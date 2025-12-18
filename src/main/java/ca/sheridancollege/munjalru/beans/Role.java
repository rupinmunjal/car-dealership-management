package ca.sheridancollege.munjalru.beans;

/**
 * Platform-level or dealership-level role.
 *
 * <ul>
 *   <li>{@link #SITE_ADMIN} — platform superuser, not tied to any dealer.</li>
 *   <li>{@link #DEALER_ADMIN} — full permissions within exactly one dealer.</li>
 *   <li>{@link #DEALER_EMPLOYEE} — scoped to one dealer; permissions
 *       determined by the {@link Permission} set assigned by a
 *       {@link #DEALER_ADMIN}.</li>
 * </ul>
 */
public enum Role {
    SITE_ADMIN,
    DEALER_ADMIN,
    DEALER_EMPLOYEE
}
