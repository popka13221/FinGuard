package com.yourname.finguard.auth.service;

import com.yourname.finguard.auth.model.User;
import com.yourname.finguard.auth.model.UserSession;
import com.yourname.finguard.auth.repository.UserSessionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSessionService {

    private final UserSessionRepository repository;
    private final int maxSessions;

    public UserSessionService(UserSessionRepository repository,
                              @Value("${app.security.sessions.max-per-user:5}") int maxSessions) {
        this.repository = repository;
        this.maxSessions = maxSessions;
    }

    @Transactional
    public void register(User user, String jti, Instant expiresAt) {
        if (jti == null || jti.isBlank() || user == null || expiresAt == null) {
            return;
        }
        repository.deleteExpired(Instant.now());
        UserSession session = new UserSession();
        session.setUser(user);
        session.setJti(jti);
        session.setExpiresAt(expiresAt);
        repository.save(session);

        List<UserSession> sessions = repository.findTop10ByUserIdOrderByCreatedAtAsc(user.getId());
        if (sessions.size() > maxSessions) {
            int toRemove = sessions.size() - maxSessions;
            for (int i = 0; i < toRemove; i++) {
                repository.delete(sessions.get(i));
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean isActive(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        Optional<UserSession> session = repository.findByJti(jti);
        return session.filter(s -> s.getExpiresAt().isAfter(Instant.now())).isPresent();
    }

    @Transactional
    public void revoke(String jti) {
        repository.findByJti(jti).ifPresent(repository::delete);
    }

    @Transactional
    public List<UserSession> revokeAll(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        List<UserSession> sessions = repository.findByUserId(user.getId());
        repository.deleteByUserId(user.getId());
        return sessions;
    }
}
