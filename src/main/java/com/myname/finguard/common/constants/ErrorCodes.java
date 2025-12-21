package com.myname.finguard.common.constants;

public final class ErrorCodes {
    private ErrorCodes() {
    }

    public static final String AUTH_INVALID_CREDENTIALS = "100001";
    public static final String AUTH_EMAIL_EXISTS = "100002";
    public static final String AUTH_WEAK_PASSWORD = "100003";
    public static final String AUTH_LOCKED = "100004";
    public static final String AUTH_REFRESH_INVALID = "100005";
    public static final String AUTH_EMAIL_NOT_VERIFIED = "100006";

    public static final String VALIDATION_GENERIC = "400001";
    public static final String VALIDATION_EMAIL = "400002";
    public static final String VALIDATION_PASSWORD = "400003";
    public static final String BAD_REQUEST = "400000";
    public static final String RATE_LIMIT = "429001";
    public static final String OTP_ALREADY_SENT = "429002";

    public static final String INTERNAL_ERROR = "900000";
}
