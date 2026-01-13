package com.myname.finguard.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RedactionTest {

    @Test
    void maskEmailHandlesNullAndShortValues() {
        assertThat(Redaction.maskEmail(null)).isEqualTo("");
        assertThat(Redaction.maskEmail("")).isEqualTo("");
        assertThat(Redaction.maskEmail("a@b")).isEqualTo("***");
        assertThat(Redaction.maskEmail("ab@b")).isEqualTo("a***@b");
        assertThat(Redaction.maskEmail("  User@Example.com ")).isEqualTo("U***@Example.com");
    }

    @Test
    void maskIpHandlesIpv4Ipv6AndGarbage() {
        assertThat(Redaction.maskIp(null)).isEqualTo("");
        assertThat(Redaction.maskIp("")).isEqualTo("");

        assertThat(Redaction.maskIp("192.168.1.2")).isEqualTo("192.168.***.***");
        assertThat(Redaction.maskIp("1.2")).isEqualTo("1.2.***.***");

        assertThat(Redaction.maskIp("2001:0db8:85a3:0000:0000:8a2e:0370:7334")).isEqualTo("2001:0db8:…");
        assertThat(Redaction.maskIp("::1")).isEqualTo("::…");

        assertThat(Redaction.maskIp("not-an-ip")).isEqualTo("***");
    }

    @Test
    void maskWalletAddressHandlesEthAndBtcFormats() {
        assertThat(Redaction.maskWalletAddress(null)).isEqualTo("");
        assertThat(Redaction.maskWalletAddress("")).isEqualTo("");
        assertThat(Redaction.maskWalletAddress("0x123")).isEqualTo("***");

        assertThat(Redaction.maskWalletAddress("0x1234567890abcdef1234567890abcdef12345678"))
                .isEqualTo("0x1234…5678");

        assertThat(Redaction.maskWalletAddress("bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh"))
                .isEqualTo("bc1q…0wlh");
    }
}

