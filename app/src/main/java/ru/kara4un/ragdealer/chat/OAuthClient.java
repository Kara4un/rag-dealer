package ru.kara4un.ragdealer.chat;

import java.util.Map;
import reactor.core.publisher.Mono;

public interface OAuthClient {
    Mono<OAuthClientResponse> token(String authorization,
                                    String contentType,
                                    String accept,
                                    String rqUid,
                                    Map<String, String> form);

    record OAuthClientResponse(int status, String body) {}
}
