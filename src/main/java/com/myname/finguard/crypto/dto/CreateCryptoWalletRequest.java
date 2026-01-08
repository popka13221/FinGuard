package com.myname.finguard.crypto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCryptoWalletRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z]{2,16}$", message = "Network must be a 2-16 letter code")
        String network,
        @NotBlank
        @Size(max = 160, message = "Address must be at most 160 characters")
        String address,
        @Size(max = 255, message = "Label must be at most 255 characters")
        String label
) {
}

