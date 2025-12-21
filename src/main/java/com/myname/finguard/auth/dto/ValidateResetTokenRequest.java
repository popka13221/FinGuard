package com.myname.finguard.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ValidateResetTokenRequest(
        @Email @NotBlank String email,
        @NotBlank String token
) {
}
