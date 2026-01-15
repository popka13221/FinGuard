package com.myname.finguard.notifications.service;

import com.myname.finguard.common.constants.ErrorCodes;
import com.myname.finguard.common.exception.ApiException;
import com.myname.finguard.notifications.dto.NotificationDto;
import com.myname.finguard.notifications.model.Notification;
import com.myname.finguard.notifications.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 200;

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationDto> listNotifications(Long userId, Integer page, Integer size) {
        if (userId == null) {
            throw unauthorized();
        }
        int pageIndex = page == null ? 0 : page;
        int pageSize = size == null ? 50 : size;
        if (pageIndex < 0) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "`page` must be >= 0", HttpStatus.BAD_REQUEST);
        }
        if (pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "`size` must be between 1 and " + MAX_PAGE_SIZE, HttpStatus.BAD_REQUEST);
        }
        var pageable = PageRequest.of(pageIndex, pageSize);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public long unreadCount(Long userId) {
        if (userId == null) {
            throw unauthorized();
        }
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    public NotificationDto updateReadStatus(Long userId, Long notificationId, Boolean read) {
        if (userId == null) {
            throw unauthorized();
        }
        if (notificationId == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "Notification id is required", HttpStatus.BAD_REQUEST);
        }
        if (read == null) {
            throw new ApiException(ErrorCodes.BAD_REQUEST, "`read` is required", HttpStatus.BAD_REQUEST);
        }
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ApiException(ErrorCodes.BAD_REQUEST, "Notification not found", HttpStatus.BAD_REQUEST));
        notification.setReadAt(read ? Instant.now() : null);
        Notification saved = notificationRepository.save(notification);
        return toDto(saved);
    }

    @Transactional
    public int bulkMarkRead(Long userId, List<Long> ids) {
        if (userId == null) {
            throw unauthorized();
        }
        Instant now = Instant.now();
        if (ids == null || ids.isEmpty()) {
            return notificationRepository.markAllRead(userId, now);
        }
        return notificationRepository.updateReadAtForIds(userId, ids, now);
    }

    private NotificationDto toDto(Notification notification) {
        Long ruleId = notification.getRule() == null ? null : notification.getRule().getId();
        return new NotificationDto(
                notification.getId(),
                ruleId,
                notification.getMessage(),
                notification.getPeriodStart(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private ApiException unauthorized() {
        return new ApiException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User is not authenticated", HttpStatus.UNAUTHORIZED);
    }
}
