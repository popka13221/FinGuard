package com.myname.finguard.common.dto;

public record ApiError(String code, String message, Long retryAfterSeconds) {
}
