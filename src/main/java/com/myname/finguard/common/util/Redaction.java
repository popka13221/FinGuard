package com.myname.finguard.common.util;

import java.util.Locale;
import org.springframework.util.StringUtils;

public final class Redaction {

    private Redaction() {
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

