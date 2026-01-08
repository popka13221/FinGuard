package com.myname.finguard.crypto.controller;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.crypto.dto.CreateCryptoWalletRequest;
import com.myname.finguard.crypto.dto.CryptoWalletDto;
import com.myname.finguard.crypto.service.CryptoWalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crypto/wallets")
@Tag(name = "Crypto Wallets", description = "Watch-only crypto wallets")
public class CryptoWalletController {

    private final CryptoWalletService cryptoWalletService;
    private final UserRepository userRepository;

    public CryptoWalletController(CryptoWalletService cryptoWalletService, UserRepository userRepository) {
        this.cryptoWalletService = cryptoWalletService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List wallets", description = "Returns watch-only crypto wallets with balances.")
    @ApiResponse(responseCode = "200", description = "Wallets returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<CryptoWalletDto>> list(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(cryptoWalletService.listWallets(userId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add wallet", description = "Adds a new watch-only wallet address for the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wallet created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CryptoWalletDto> create(
            @Valid @RequestBody CreateCryptoWalletRequest request,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        CryptoWalletDto created = cryptoWalletService.createWallet(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Archive wallet", description = "Archives a watch-only wallet for the current user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Wallet archived"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> archive(@PathVariable Long id, Authentication authentication) {
        Long userId = resolveUserId(authentication);
        cryptoWalletService.archiveWallet(userId, id);
        return ResponseEntity.noContent().build();
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

