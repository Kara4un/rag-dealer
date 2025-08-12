package ru.kara4un.ragdealer.chat;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.MediaType;
import jakarta.inject.Singleton;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TokenManager {
    private static final Logger LOG = LoggerFactory.getLogger(TokenManager.class);

    private final OAuthClient oauthClient;
    private final String scope;
    private final long proactiveRefreshSeconds;
    private final String authorizationKey;
    private final String authorizationScheme;
    private final ObjectMapper objectMapper;
    private final boolean logAuthorization;

    private volatile String accessToken;
    private volatile Instant expiresAt;
    private final AtomicReference<Mono<String>> inFlightRefresh = new AtomicReference<>();

    public TokenManager(
            OAuthClient oauthClient,
            @Value("${gigachat.oauth.scope}") String scope,
            @Value("${gigachat.token.proactive-refresh-seconds}") long proactiveRefreshSeconds,
            @Value("${gigachat.oauth.authorization-key:}") String authorizationKey,
            @Value("${gigachat.oauth.authorization-scheme:Bearer}") String authorizationScheme,
            @Value("${gigachat.oauth.log-authorization:false}") boolean logAuthorization,
            ObjectMapper objectMapper) {
        this.oauthClient = oauthClient;
        this.scope = scope;
        this.proactiveRefreshSeconds = proactiveRefreshSeconds;
        this.authorizationKey = authorizationKey;
        // По умолчанию используем Bearer; Basic не применяется
        this.authorizationScheme = (authorizationScheme == null || authorizationScheme.isBlank()) ? "Bearer" : authorizationScheme.trim();
        this.objectMapper = objectMapper;
        this.logAuthorization = logAuthorization;
    }

    // Backward-compatible convenience constructor (no ObjectMapper provided)
    public TokenManager(
            OAuthClient oauthClient,
            String scope,
            long proactiveRefreshSeconds,
            String authorizationKey) {
        this(oauthClient, scope, proactiveRefreshSeconds, authorizationKey, new ObjectMapper());
    }

    // Convenience constructor used in tests (explicit ObjectMapper)
    public TokenManager(
            OAuthClient oauthClient,
            String scope,
            long proactiveRefreshSeconds,
            String authorizationKey,
            ObjectMapper objectMapper) {
        this(oauthClient, scope, proactiveRefreshSeconds, authorizationKey, "Bearer", false, objectMapper);
    }

    public Mono<String> getValidTokenReactive() {
        if (isTokenValid()) {
            return Mono.just(accessToken);
        }
        Mono<String> existing = inFlightRefresh.get();
        if (existing != null) {
            return existing;
        }
        Mono<String> refresher = refreshTokenReactive()
                .doFinally(sig -> inFlightRefresh.set(null))
                .cache();
        if (inFlightRefresh.compareAndSet(null, refresher)) {
            return refresher;
        }
        return inFlightRefresh.get();
    }

    public Mono<String> refreshTokenReactive() {
        if (authorizationKey == null || authorizationKey.isBlank()) {
            return Mono.error(new IllegalStateException("GIGACHAT_AUTHORIZATION_KEY is not set"));
        }
        String authHeader = authorizationScheme + " " + authorizationKey.trim();
        String rqUid = UUID.randomUUID().toString();
        if (logAuthorization) {
            LOG.info("OAuth Authorization header: {}", authHeader);
        }
        Publisher<io.micronaut.http.HttpResponse<String>> call = oauthClient.token(
                authHeader,
                MediaType.APPLICATION_FORM_URLENCODED,
                MediaType.ALL,
                rqUid,
                (scope == null || scope.isBlank()) ? java.util.Collections.emptyMap() : java.util.Collections.singletonMap("scope", scope));
        return Mono.from(call)
                .flatMap(resp -> {
                    if (resp.getStatus() == HttpStatus.OK) {
                        return Mono.just(resp.getBody(String.class).orElse(""));
                    }
                    String errBody = resp.getBody(String.class).orElse("");
                    if (resp.getStatus() == HttpStatus.UNAUTHORIZED) {
                        LOG.warn("OAuth unauthorized: status={} body={}", resp.getStatus(), errBody);
                        String retryAuth = authHeader;
                        if (logAuthorization) {
                            LOG.info("OAuth Authorization header (retry): {}", retryAuth);
                        }
                        return Mono.from(oauthClient.token(
                                retryAuth,
                                MediaType.APPLICATION_FORM_URLENCODED,
                                MediaType.ALL,
                                UUID.randomUUID().toString(),
                                (scope == null || scope.isBlank()) ? java.util.Collections.emptyMap() : java.util.Collections.singletonMap("scope", scope)))
                                .flatMap(resp2 -> {
                                    if (resp2.getStatus() == HttpStatus.OK) {
                                        return Mono.just(resp2.getBody(String.class).orElse(""));
                                    }
                                    String errBody2 = resp2.getBody(String.class).orElse("");
                                    LOG.error("OAuth retry failed: status={} body={}", resp2.getStatus(), errBody2);
                                    return Mono.error(new IllegalStateException("OAuth error after retry: " + resp2.getStatus()));
                                });
                    }
                    LOG.error("OAuth request failed: status={} body={}", resp.getStatus(), errBody);
                    return Mono.error(new IllegalStateException("OAuth error: " + resp.getStatus()));
                })
                .map(body -> {
                    try {
                        JsonNode root = objectMapper.readTree(body);
                        this.accessToken = root.path("access_token").asText(null);
                        if (this.accessToken == null || this.accessToken.isEmpty()) {
                            throw new IllegalStateException("OAuth response missing access_token");
                        }
                        if (root.hasNonNull("expires_at")) {
                            JsonNode expNode = root.get("expires_at");
                            // Поддерживаем несколько форматов: ISO-8601, epoch seconds, epoch millis
                            if (expNode.isNumber()) {
                                long v = expNode.asLong();
                                // Heuristic: > 1e12 => миллисекунды, иначе секунды
                                this.expiresAt = (v > 1000_000_000_000L)
                                        ? Instant.ofEpochMilli(v)
                                        : Instant.ofEpochSecond(v);
                            } else {
                                String text = expNode.asText();
                                try {
                                    // Пытаемся как ISO-8601
                                    this.expiresAt = Instant.parse(text);
                                } catch (Exception dtpe) {
                                    // Если пришло строкой-числом
                                    try {
                                        long v = Long.parseLong(text);
                                        this.expiresAt = (v > 1000_000_000_000L)
                                                ? Instant.ofEpochMilli(v)
                                                : Instant.ofEpochSecond(v);
                                    } catch (NumberFormatException nfe) {
                                        throw new IllegalStateException("Unsupported expires_at format: " + text, nfe);
                                    }
                                }
                            }
                        } else if (root.hasNonNull("expires_in")) {
                            this.expiresAt = Instant.now().plusSeconds(root.get("expires_in").asLong());
                        } else {
                            this.expiresAt = Instant.now().plusSeconds(1800);
                        }
                        return this.accessToken;
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to parse OAuth response", ex);
                    }
                });
    }

    private boolean isTokenValid() {
        Instant exp = this.expiresAt;
        String token = this.accessToken;
        return token != null && exp != null && exp.minusSeconds(proactiveRefreshSeconds).isAfter(Instant.now());
    }

    public String getCachedToken() {
        return this.accessToken;
    }
}
