package com.example.Finance_Tracker.User.dto;

/**
 * Single source of the password policy for every request that sets a password
 * (registration and password reset). Annotation attributes need compile-time
 * constants, so the rule is expressed as constants rather than a method.
 */
public final class PasswordRules {

    public static final int MIN_LENGTH = 8;

    /** At least one lowercase, one uppercase, one digit and one of @$!%*?&; only those characters allowed. */
    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    public static final String LENGTH_MESSAGE = "Password must be at least 8 characters long";

    public static final String PATTERN_MESSAGE =
            "Password must contain at least one lowercase, uppercase, numbers and special character";

    private PasswordRules() {
    }
}
