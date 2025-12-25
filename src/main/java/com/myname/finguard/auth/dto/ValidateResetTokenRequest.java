package com.myname.finguard.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateResetTokenRequest(
        String email,
        @NotBlank String token
) {
}
