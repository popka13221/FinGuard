package com.yourname.finguard.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateResetTokenRequest(@NotBlank String token) {
}
