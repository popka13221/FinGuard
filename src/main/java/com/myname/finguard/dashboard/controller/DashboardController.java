package com.myname.finguard.dashboard.controller;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.dashboard.dto.AccountIntelligenceResponse;
import com.myname.finguard.dashboard.dto.DashboardOverviewResponse;
import com.myname.finguard.dashboard.dto.UpcomingPaymentDto;
import com.myname.finguard.dashboard.service.AccountIntelligenceService;
import com.myname.finguard.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard aggregated snapshot")
public class DashboardController {

    private final DashboardService dashboardService;
    private final AccountIntelligenceService accountIntelligenceService;
    private final UserRepository userRepository;

    public DashboardController(
            DashboardService dashboardService,
            AccountIntelligenceService accountIntelligenceService,
            UserRepository userRepository
    ) {
        this.dashboardService = dashboardService;
        this.accountIntelligenceService = accountIntelligenceService;
        this.userRepository = userRepository;
    }

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Dashboard overview", description = "Returns a consistent dashboard snapshot in one payload.")
    @ApiResponse(responseCode = "200", description = "Overview returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DashboardOverviewResponse> overview(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(dashboardService.overview(userId));
    }

    @GetMapping("/upcoming-payments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upcoming payments", description = "Returns recurring payment candidates from wallet insights.")
    @ApiResponse(responseCode = "200", description = "Upcoming payments returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<UpcomingPaymentDto>> upcomingPayments(
            @RequestParam(name = "limit", required = false, defaultValue = "5") int limit,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(dashboardService.upcomingPayments(userId, limit));
    }

    @GetMapping("/account-intelligence")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Account intelligence",
            description = "Returns account-level intelligence for net worth, series, allocation and insights."
    )
    @ApiResponse(responseCode = "200", description = "Account intelligence returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AccountIntelligenceResponse> accountIntelligence(
            @RequestParam(name = "window", required = false, defaultValue = "30d") String window,
            @RequestParam(name = "metric", required = false, defaultValue = "net") String metric,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(accountIntelligenceService.accountIntelligence(userId, window, metric));
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
