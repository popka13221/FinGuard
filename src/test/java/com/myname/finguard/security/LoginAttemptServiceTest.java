package com.myname.finguard.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.myname.finguard.security.model.LoginAttempt;
import com.myname.finguard.security.repository.LoginAttemptRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LoginAttemptServiceTest {

    @Test
    void recordFailureLocksAndResetsAttemptsWhenThresholdReached() {
        LoginAttemptRepository repo = mock(LoginAttemptRepository.class);
        LoginAttemptService service = new LoginAttemptService(repo, 2, 15);

        LoginAttempt existing = new LoginAttempt();
        existing.setEmail("user@example.com");
        existing.setAttempts(1);
        when(repo.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(existing));

        service.recordFailure("USER@EXAMPLE.COM");

        ArgumentCaptor<LoginAttempt> captor = ArgumentCaptor.forClass(LoginAttempt.class);
        verify(repo).save(captor.capture());
        LoginAttempt saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getAttempts()).isZero();
        assertThat(saved.getLockUntil()).isNotNull();
        assertThat(saved.getLockUntil()).isAfter(Instant.now());
    }

    @Test
    void isLockedReturnsTrueWhenLockNotExpired() {
        LoginAttemptRepository repo = mock(LoginAttemptRepository.class);
        LoginAttemptService service = new LoginAttemptService(repo, 5, 15);

        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail("user@example.com");
        attempt.setLockUntil(Instant.now().plusSeconds(60));
        when(repo.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(attempt));

        assertThat(service.isLocked("USER@EXAMPLE.COM")).isTrue();
        verify(repo, never()).deleteById(any());
    }

    @Test
    void isLockedDeletesAndReturnsFalseWhenLockExpired() {
        LoginAttemptRepository repo = mock(LoginAttemptRepository.class);
        LoginAttemptService service = new LoginAttemptService(repo, 5, 15);

        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail("user@example.com");
        attempt.setLockUntil(Instant.now().minusSeconds(60));
        when(repo.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(attempt));

        assertThat(service.isLocked("USER@EXAMPLE.COM")).isFalse();
        verify(repo).deleteById(eq("user@example.com"));
    }

    @Test
    void lockRemainingSecondsReturnsZeroWhenNoLock() {
        LoginAttemptRepository repo = mock(LoginAttemptRepository.class);
        LoginAttemptService service = new LoginAttemptService(repo, 5, 15);

        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail("user@example.com");
        attempt.setLockUntil(null);
        when(repo.findByEmail("user@example.com")).thenReturn(java.util.Optional.of(attempt));

        assertThat(service.lockRemainingSeconds("user@example.com")).isZero();
        verify(repo, never()).deleteById(any());
    }
}

