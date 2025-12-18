package ca.sheridancollege.munjalru.beans;

/**
 * Granular permissions assignable to {@link Role#DEALER_EMPLOYEE} users
 * by their {@link Role#DEALER_ADMIN}.
 *
 * <p>{@link Role#SITE_ADMIN} and {@link Role#DEALER_ADMIN} implicitly hold
 * all permissions; this set is only meaningful for DEALER_EMPLOYEE.</p>
 */
public enum Permission {
    CAN_ADD_CAR,
    CAN_EDIT_CAR,
    CAN_DELETE_CAR
}
