package com.myname.finguard.auth.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        String baseCurrency,
        String role
) {
}
