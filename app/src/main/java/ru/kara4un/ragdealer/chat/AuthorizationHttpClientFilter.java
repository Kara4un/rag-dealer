package ru.kara4un.ragdealer.chat;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import jakarta.inject.Provider;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Filter("/**")
public class AuthorizationHttpClientFilter implements HttpClientFilter {

    private final Provider<TokenManager> tokenManager;
    private final String apiBaseUrl;

    public AuthorizationHttpClientFilter(Provider<TokenManager> tokenManager,
                                         @Value("${gigachat.api.base-url}") String apiBaseUrl) {
        this.tokenManager = tokenManager;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        String uri = request.getUri().toString();
        if (!uri.startsWith(apiBaseUrl)) {
            @SuppressWarnings("unchecked")
            Publisher<HttpResponse<?>> proceed = (Publisher<HttpResponse<?>>) chain.proceed(request);
            return proceed;
        }

        return Mono.defer(() -> tokenManager.get().getValidTokenReactive())
                .flatMap(token -> {
                    request.bearerAuth(token);
                    @SuppressWarnings("unchecked")
                    Publisher<HttpResponse<?>> publisher = (Publisher<HttpResponse<?>>) chain.proceed(request);
                    return Mono.from(publisher);
                })
                .onErrorResume(HttpClientResponseException.class, e -> {
                    if (e.getStatus() == HttpStatus.UNAUTHORIZED) {
                        return tokenManager.get().refreshTokenReactive()
                                .flatMap(token -> {
                                    request.bearerAuth(token);
                                    @SuppressWarnings("unchecked")
                                    Publisher<HttpResponse<?>> retry = (Publisher<HttpResponse<?>>) chain.proceed(request);
                                    return Mono.from(retry);
                                });
                    }
                    return Mono.error(e);
                });
    }
}
