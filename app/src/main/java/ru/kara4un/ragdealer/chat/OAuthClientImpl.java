package ru.kara4un.ragdealer.chat;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OAuthClientImpl implements OAuthClient {

    private final WebClient client;

    public OAuthClientImpl(WebClient.Builder builder,
                           @Value("${gigachat.oauth.base-url}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<OAuthClientResponse> token(String authorization,
                                           String contentType,
                                           String accept,
                                           String rqUid,
                                           Map<String, String> form) {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (form != null) {
            form.forEach(body::add);
        }
        return client.post()
                .uri("/oauth")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("RqUID", rqUid)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.ALL)
                .body(BodyInserters.fromFormData(body))
                .exchangeToMono(resp -> resp.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(b -> new OAuthClientResponse(resp.statusCode().value(), b)));
    }
}
