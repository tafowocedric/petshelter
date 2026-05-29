package com.petshelter.web.session;

import com.petshelter.model.User;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    public static final String COOKIE_NAME = "PSSESSION";
    private static final Duration SESSION_TTL = Duration.ofHours(2);
    private static final SecureRandom RNG = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public Session create(User user) {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Session s = new Session(token, user);
        sessions.put(token, s);
        return s;
    }

    public Optional<Session> get(String token) {
        if (token == null) return Optional.empty();

        Session s = sessions.get(token);

        if (s == null) return Optional.empty();
        if (isExpired(s)) {
            sessions.remove(token);
            return Optional.empty();
        }
        s.touch();
        return Optional.of(s);
    }

    public void destroy(String token) {
        if (token != null) sessions.remove(token);
    }

    private boolean isExpired(Session s) {
        return s.lastAccessed().plus(SESSION_TTL).isBefore(Instant.now());
    }
}