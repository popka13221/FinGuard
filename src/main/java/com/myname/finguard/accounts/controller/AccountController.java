package com.myname.finguard.accounts.controller;

import com.myname.finguard.accounts.dto.CreateAccountRequest;
import com.myname.finguard.accounts.dto.UserBalanceResponse;
import com.myname.finguard.accounts.service.AccountService;
import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Accounts and balances")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    public AccountController(AccountService accountService, UserRepository userRepository) {
        this.accountService = accountService;
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "User balance", description = "Returns the current user's account balances. "
            + "Archived accounts are excluded from the aggregate.")
    @ApiResponse(responseCode = "200", description = "Balance returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserBalanceResponse> balance(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(accountService.getUserBalance(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create account", description = "Creates a new account for the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserBalanceResponse.AccountBalance> create(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        UserBalanceResponse.AccountBalance created = accountService.createAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
