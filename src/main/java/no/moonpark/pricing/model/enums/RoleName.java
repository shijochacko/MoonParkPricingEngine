package no.moonpark.pricing.model.enums;

/**
 * Built-in roles. A person can have several roles; an API client always has exactly {@link #EXTERNAL_CLIENT}.
 */
public enum RoleName {
    /** Registered person: calculate fees, read own profile. */
    USER,
    /** Third-party system authenticated with an API key: calculate fees (single and batch). */
    EXTERNAL_CLIENT,
    /** MoonPark staff: manage tariffs and API clients, read users and audit history. */
    ADMIN,
    /** Platform owner: everything, including user and role management. */
    SUPER_ADMIN
}
