package com.yourname.finguard.common.util;

import java.util.Set;

public final class PasswordValidator {

    private static final Set<String> BANNED = Set.of(
            "password", "123456", "123456789", "qwerty", "111111", "12345678", "abc123", "1234567",
            "qwerty123", "1q2w3e4r", "123123", "000000", "password1", "iloveyou", "admin", "welcome"
    );

    private PasswordValidator() {
    }

    public static boolean isWeak(String password) {
        if (password == null) return true;
        String lower = password.toLowerCase();
        return BANNED.contains(lower) || lower.contains("password") || lower.contains("qwerty");
    }
}
