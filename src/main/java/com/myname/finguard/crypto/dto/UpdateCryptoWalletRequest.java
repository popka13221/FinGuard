package com.myname.finguard.crypto.dto;

import jakarta.validation.constraints.Size;

public record UpdateCryptoWalletRequest(
        @Size(max = 255, message = "Label must be at most 255 characters")
        String label
) {
}
