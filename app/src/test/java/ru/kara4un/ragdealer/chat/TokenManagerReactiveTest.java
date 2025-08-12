package ru.kara4un.ragdealer.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Header;
import java.util.concurrent.atomic.AtomicInteger;
import io.micronaut.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TokenManagerReactiveTest {

    static class FakeOAuthClient implements OAuthClient {
        private final AtomicInteger calls = new AtomicInteger();
        private volatile String token = "T1";

        void setToken(String token) {
            this.token = token;
        }

        int getCalls() {
            return calls.get();
        }

        @Override
        public org.reactivestreams.Publisher<HttpResponse<String>> token(
                @Header(HttpHeaders.AUTHORIZATION) String authorization,
                @Header(HttpHeaders.CONTENT_TYPE) String contentType,
                @Header(HttpHeaders.ACCEPT) String accept,
                @Header("RqUID") String rqUid,
                java.util.Map<String, String> form) {
            calls.incrementAndGet();
            String json = "{\"access_token\":\"" + token + "\",\"expires_in\":3600}";
            return Mono.just(HttpResponse.ok(json));
        }
    }

    @Test
    void getValidTokenReactive_fetchesAndCachesUntilExpiry() {
        FakeOAuthClient oauth = new FakeOAuthClient();
        TokenManager manager = new TokenManager(oauth, "scope", 60, "AUTHKEY", new ObjectMapper());

        StepVerifier.create(manager.getValidTokenReactive())
                .expectNext("T1")
                .verifyComplete();

        // Second call should return cached token without invoking OAuth again
        StepVerifier.create(manager.getValidTokenReactive())
                .expectNext("T1")
                .verifyComplete();

        assertEquals(1, oauth.getCalls(), "OAuth should be called once for valid cached token");

        // New manager (no cached token) should fetch again and see updated token
        oauth.setToken("T2");
        TokenManager manager2 = new TokenManager(oauth, "scope", 60, "AUTHKEY", new ObjectMapper());
        StepVerifier.create(manager2.getValidTokenReactive())
                .expectNext("T2")
                .verifyComplete();
        assertEquals(2, oauth.getCalls(), "OAuth should be called again for a new manager instance");
    }
}
