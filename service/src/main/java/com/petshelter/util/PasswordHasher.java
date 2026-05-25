package com.petshelter.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility class for password hashing using SHA-256.
 * [ENCAPSULATION] [DATA TYPES] [EXCEPTIONS]
 */
public final class PasswordHasher {
    private static final int COST = 12;

    private PasswordHasher() {
        throw new AssertionError("PasswordHasher is a utility class — do not instantiate.");
    }

    /**
     * Hashes a plain-text password and returns a BCrypt hash string.
     */
    public static String hash(String plain) {
        if (plain == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.withDefaults().hashToString(COST, plain.toCharArray());
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     * @return true if the password matches the hash
     */
    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null) {
            return false;
        }
        try {
            BCrypt.Result result = BCrypt.verifyer().verify(plain.toCharArray(), hash);
            return result.verified;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
