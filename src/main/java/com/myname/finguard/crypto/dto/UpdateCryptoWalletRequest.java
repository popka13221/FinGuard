package com.myname.finguard.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCryptoWalletRequest(
        @NotBlank(message = "Label is required")
        @Size(max = 120, message = "Label must be at most 120 characters")
        @Pattern(regexp = "^[^\\p{Cntrl}]+$", message = "Label contains unsupported characters")
        String label
) {
}
