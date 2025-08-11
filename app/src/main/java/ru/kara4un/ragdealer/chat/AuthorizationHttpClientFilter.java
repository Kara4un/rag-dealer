package ru.kara4un.ragdealer.chat;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.http.annotation.Filter;
import jakarta.inject.Provider;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

@Filter("/chat/**")
public class AuthorizationHttpClientFilter implements HttpClientFilter {

    private final Provider<TokenManager> tokenManager;

    public AuthorizationHttpClientFilter(Provider<TokenManager> tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        request.bearerAuth(tokenManager.get().getValidToken());
        @SuppressWarnings("unchecked")
        Publisher<HttpResponse<?>> publisher = (Publisher<HttpResponse<?>>) chain.proceed(request);
        return Mono.from(publisher)
                .onErrorResume(HttpClientResponseException.class, e -> {
                    if (e.getStatus() == HttpStatus.UNAUTHORIZED) {
                        tokenManager.get().refreshToken();
                        request.bearerAuth(tokenManager.get().getValidToken());
                        @SuppressWarnings("unchecked")
                        Publisher<HttpResponse<?>> retry = (Publisher<HttpResponse<?>>) chain.proceed(request);
                        return Mono.from(retry);
                    }
                    return Mono.error(e);
                });
    }
}
