package com.myname.finguard.notifications.controller;

import com.myname.finguard.auth.model.User;
import com.myname.finguard.auth.repository.UserRepository;
import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.notifications.dto.BulkMarkReadRequest;
import com.myname.finguard.notifications.dto.BulkMarkReadResponse;
import com.myname.finguard.notifications.dto.NotificationDto;
import com.myname.finguard.notifications.dto.UnreadCountResponse;
import com.myname.finguard.notifications.dto.UpdateNotificationRequest;
import com.myname.finguard.notifications.service.NotificationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "User alerts and notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List notifications", description = "Returns paged notifications for the current user.")
    @ApiResponse(responseCode = "200", description = "Notifications returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<NotificationDto>> list(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(notificationService.listNotifications(userId, page, size));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Unread count", description = "Returns unread notifications count.")
    @ApiResponse(responseCode = "200", description = "Unread count returned")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UnreadCountResponse> unreadCount(Authentication authentication) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(new UnreadCountResponse(notificationService.unreadCount(userId)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark read/unread", description = "Marks a notification as read or unread.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<NotificationDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotificationRequest request,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        return ResponseEntity.ok(notificationService.updateReadStatus(userId, id, request.read()));
    }

    @PostMapping("/mark-read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Bulk mark read", description = "Marks multiple notifications as read. When ids are omitted, marks all unread.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BulkMarkReadResponse> bulkMarkRead(
            @RequestBody(required = false) BulkMarkReadRequest request,
            Authentication authentication
    ) {
        Long userId = resolveUserId(authentication);
        List<Long> ids = request == null ? null : request.ids();
        int updated = notificationService.bulkMarkRead(userId, ids);
        return ResponseEntity.status(HttpStatus.OK).body(new BulkMarkReadResponse(updated));
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
