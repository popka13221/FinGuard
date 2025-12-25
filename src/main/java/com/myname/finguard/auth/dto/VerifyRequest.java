package com.myname.finguard.auth.dto;

public record VerifyRequest(
        String email,
        String token
) {
}
