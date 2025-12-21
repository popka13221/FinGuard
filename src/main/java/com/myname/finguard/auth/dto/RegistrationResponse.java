package com.myname.finguard.auth.dto;

public record RegistrationResponse(
        boolean verificationRequired,
        String message,
        String token
) {
}
