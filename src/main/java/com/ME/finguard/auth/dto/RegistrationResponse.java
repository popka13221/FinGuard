package com.yourname.finguard.auth.dto;

public record RegistrationResponse(
        boolean verificationRequired,
        String message,
        String token
) {
}
