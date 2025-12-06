package com.yourname.finguard.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank
        @Size(min = 10, max = 100, message = "Password must be at least 10 characters long")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "Password must include upper and lower case letters, a digit, and a special character"
        )
        String password,
        @NotBlank
        @Size(max = 120, message = "Full name must be at most 120 characters")
        String fullName,
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{3,5}$", message = "Base currency must be a 3-5 letter code")
        String baseCurrency
) {
}
