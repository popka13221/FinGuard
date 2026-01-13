package com.myname.finguard.reports.controller;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.reports.dto.CashFlowResponse;
import com.myname.finguard.reports.dto.ReportPeriod;
import com.myname.finguard.reports.dto.ReportSummaryResponse;
import com.myname.finguard.reports.dto.ReportsByCategoryResponse;
import com.myname.finguard.reports.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Aggregated reports for transactions")
public class ReportsController {

    private final ReportsService reportsService;
    private final UserRepository userRepository;

    public ReportsController(ReportsService reportsService, UserRepository userRepository) {
        this.reportsService = reportsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Report summary", description = "Aggregates total income, expense, and net cash flow.")
    @ApiResponse(responseCode = "200", description = "Summary returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReportSummaryResponse> summary(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant to,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(reportsService.summary(userId, parsePeriod(period), from, to));
    }

    @GetMapping("/by-category")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Report by category", description = "Aggregates income and expense totals grouped by category.")
    @ApiResponse(responseCode = "200", description = "Report returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReportsByCategoryResponse> byCategory(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(reportsService.byCategory(userId, parsePeriod(period), from, to, limit));
    }

    @GetMapping("/cash-flow")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cash flow time series", description = "Returns daily income/expense/net cash flow series.")
    @ApiResponse(responseCode = "200", description = "Report returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CashFlowResponse> cashFlow(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Instant to,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(reportsService.cashFlow(userId, from, to));
    }

    private ReportPeriod parsePeriod(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String normalized = raw.trim().toUpperCase();
        try {
            return ReportPeriod.valueOf(normalized);
        } catch (Exception e) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Unsupported period: " + normalized, HttpStatus.BAD_REQUEST);
        }
    }

    private Long resolveUserId(Authentication authentication) {
        if (authentication == null
                || authentication.getPrincipal() == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw unauthorized();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.myname.finguard.security.UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(User::getId)
                    .orElseThrow(this::unauthorized);
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElseThrow(this::unauthorized);
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }
}

