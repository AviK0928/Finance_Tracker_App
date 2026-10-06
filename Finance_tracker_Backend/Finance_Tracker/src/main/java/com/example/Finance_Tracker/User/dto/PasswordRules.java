package com.example.Finance_Tracker.User.dto;

/**
 * Single source of the password policy for every request that sets a password
 * (registration and password reset). Annotation attributes need compile-time
 * constants, so the rule is expressed as constants rather than a method.
 */
public final class PasswordRules {

    public static final int MIN_LENGTH = 8;

    /**
     * BCrypt only uses the first 72 bytes, so a longer password would match any password that
     * shares those bytes. PATTERN allows ASCII characters only, so 72 characters are 72 bytes.
     */
    public static final int MAX_LENGTH = 72;

    /** At least one lowercase, one uppercase, one digit and one of @$!%*?&; only those characters allowed. */
    public static final String PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    public static final String LENGTH_MESSAGE = "Password must be 8 to 72 characters long";

    public static final String PATTERN_MESSAGE =
            "Password must contain at least one lowercase, uppercase, numbers and special character";

    private PasswordRules() {
    }
}
