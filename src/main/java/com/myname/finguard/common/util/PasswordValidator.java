package com.myname.finguard.common.util;

import java.util.Set;
import java.util.regex.Pattern;

public final class PasswordValidator {

    private static final Set<String> BANNED = Set.of(
            "password", "123456", "123456789", "qwerty", "111111", "12345678", "abc123", "1234567",
            "qwerty123", "1q2w3e4r", "123123", "000000", "password1", "iloveyou", "admin", "welcome"
    );
    private static final Pattern STRONG = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}$");

    private PasswordValidator() {
    }

    public static boolean isWeak(String password) {
        if (password == null) return true;
        String lower = password.toLowerCase();
        if (!STRONG.matcher(password).matches()) {
            return true;
        }
        return BANNED.contains(lower) || lower.contains("password") || lower.contains("qwerty");
    }
}
