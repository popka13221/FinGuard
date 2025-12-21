package com.myname.finguard.auth.dto;

public record OtpChallengeResponse(boolean otpRequired, long expiresInSeconds) {
}
