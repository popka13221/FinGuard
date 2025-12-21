package com.myname.finguard.auth.dto;

public record ResetSessionResponse(String resetSessionToken, long expiresInSeconds) {
}
