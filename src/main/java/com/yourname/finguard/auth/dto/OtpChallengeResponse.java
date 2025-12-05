package com.yourname.finguard.auth.dto;

public record OtpChallengeResponse(boolean otpRequired, long expiresInSeconds) {
}
