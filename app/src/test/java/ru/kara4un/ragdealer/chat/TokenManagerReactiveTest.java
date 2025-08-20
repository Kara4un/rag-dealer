package ru.kara4un.ragdealer.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
        public Mono<OAuthClientResponse> token(String authorization,
                                               String contentType,
                                               String accept,
                                               String rqUid,
                                               Map<String, String> form) {
            calls.incrementAndGet();
            String json = "{\"access_token\":\"" + token + "\",\"expires_in\":3600}";
            return Mono.just(new OAuthClientResponse(200, json));
        }
    }

    @Test
    void getValidTokenReactive_fetchesAndCachesUntilExpiry() {
        FakeOAuthClient oauth = new FakeOAuthClient();
        TokenManager manager = new TokenManager(oauth, "scope", 60, "AUTHKEY", new ObjectMapper());

        StepVerifier.create(manager.getValidTokenReactive())
                .expectNext("T1")
                .verifyComplete();

        StepVerifier.create(manager.getValidTokenReactive())
                .expectNext("T1")
                .verifyComplete();

        assertEquals(1, oauth.getCalls(), "OAuth should be called once for valid cached token");

        oauth.setToken("T2");
        TokenManager manager2 = new TokenManager(oauth, "scope", 60, "AUTHKEY", new ObjectMapper());
        StepVerifier.create(manager2.getValidTokenReactive())
                .expectNext("T2")
                .verifyComplete();
        assertEquals(2, oauth.getCalls(), "OAuth should be called again for a new manager instance");
    }
}
