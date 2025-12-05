package com.yourname.finguard.auth.dto;

public record ResetSessionResponse(String resetSessionToken, long expiresInSeconds) {
}
