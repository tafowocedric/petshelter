package com.petshelter.web.session;

import com.petshelter.model.User;
import java.time.Instant;

public class Session {
    private final String token;
    private final User user;
    private Instant lastAccessed;

    public Session(String token, User user) {
        this.token = token;
        this.user = user;
        this.lastAccessed = Instant.now();
    }

    public String token() { return token; }
    public User user() { return user; }
    public Instant lastAccessed() { return lastAccessed; }
    public void touch() { this.lastAccessed = Instant.now(); }
}