package com.myname.finguard.common.util;

import java.util.Locale;
import org.springframework.util.StringUtils;

public final class Redaction {

    private Redaction() {
    }

    public static String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        String raw = email.trim();
        int at = raw.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return raw.charAt(0) + "***" + raw.substring(at);
    }

    public static String maskIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "";
        }
        String raw = ip.trim();
        if (raw.contains(".")) {
            String[] parts = raw.split("\\.");
            if (parts.length >= 2) {
                return parts[0] + "." + parts[1] + ".***.***";
            }
            return "***.***.***.***";
        }
        if (raw.contains(":")) {
            String[] parts = raw.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1] + ":…";
            }
            return "***:…";
        }
        return "***";
    }

    public static String maskWalletAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return "";
        }
        String raw = address.trim();
        if (raw.length() <= 10) {
            return "***";
        }
        int prefix = raw.toLowerCase(Locale.ROOT).startsWith("0x") ? 6 : 4;
        if (raw.length() <= prefix + 4) {
            return "***";
        }
        return raw.substring(0, prefix) + "…" + raw.substring(raw.length() - 4);
    }
}
