package ru.kara4un.ragdealer.chat;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class TokenManager {

    private final OAuthClient oauthClient;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final long proactiveRefreshSeconds;
    private final ReentrantLock lock = new ReentrantLock();

    private String accessToken;
    private Instant expiresAt;

    public TokenManager(
            OAuthClient oauthClient,
            @Value("${gigachat.oauth.client-id}") String clientId,
            @Value("${gigachat.oauth.client-secret}") String clientSecret,
            @Value("${gigachat.oauth.scope}") String scope,
            @Value("${gigachat.token.proactive-refresh-seconds}") long proactiveRefreshSeconds) {
        this.oauthClient = oauthClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.proactiveRefreshSeconds = proactiveRefreshSeconds;
    }

    public String getValidToken() {
        lock.lock();
        try {
            if (accessToken == null || expiresAt == null ||
                    expiresAt.minusSeconds(proactiveRefreshSeconds).isBefore(Instant.now())) {
                refreshTokenInternal();
            }
            return accessToken;
        } finally {
            lock.unlock();
        }
    }

    public void refreshToken() {
        lock.lock();
        try {
            refreshTokenInternal();
        } finally {
            lock.unlock();
        }
    }

    private void refreshTokenInternal() {
        String credentials = clientId + ":" + clientSecret;
        String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        OAuthClient.TokenResponse response;
        try {
            response = oauthClient.token(authHeader, "scope=" + scope);
        } catch (HttpClientResponseException e) {
            if (e.getStatus() == HttpStatus.UNAUTHORIZED) {
                response = oauthClient.token(authHeader, "scope=" + scope);
            } else {
                throw e;
            }
        }
        this.accessToken = response.accessToken();
        if (response.expiresAt() != null) {
            this.expiresAt = Instant.parse(response.expiresAt());
        } else if (response.expiresIn() != null) {
            this.expiresAt = Instant.now().plusSeconds(response.expiresIn());
        } else {
            this.expiresAt = Instant.now().plusSeconds(1800);
        }
    }
}
