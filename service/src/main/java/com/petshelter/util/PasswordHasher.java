package com.petshelter.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

// BCrypt password hashing utilities.
public final class PasswordHasher {
    private static final int COST = 12;

    private PasswordHasher() {
        throw new AssertionError("PasswordHasher is a utility class — do not instantiate.");
    }

    public static String hash(String plain) {
        if (plain == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.withDefaults().hashToString(COST, plain.toCharArray());
    }

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
