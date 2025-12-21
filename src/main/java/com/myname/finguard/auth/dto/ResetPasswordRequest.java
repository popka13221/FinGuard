package com.myname.finguard.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String resetSessionToken,
        @NotBlank
        @Size(min = 10, max = 100, message = "Password must be at least 10 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Password must include upper and lower case letters, a digit, and a special character"
        )
        String password
) {
}
