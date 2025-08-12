package ru.kara4un.ragdealer.chat;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import java.net.URI;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Filter("/**")
public class NgwAuthorizationLoggingFilter implements HttpClientFilter {

    private static final Logger LOG = LoggerFactory.getLogger(NgwAuthorizationLoggingFilter.class);

    private final String oauthBaseUrl;
    private final boolean enabled;

    public NgwAuthorizationLoggingFilter(
            @Value("${gigachat.oauth.base-url}") String oauthBaseUrl,
            @Value("${gigachat.oauth.log-authorization:false}") boolean enabled) {
        this.oauthBaseUrl = oauthBaseUrl;
        this.enabled = enabled;
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        if (enabled) {
            URI uri = request.getUri();
            String s = uri.toString();
            // Логируем только обращения к OAuth (ngw) базовому URL
            if (s.startsWith(oauthBaseUrl)) {
                String auth = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
                LOG.info("OAuth Authorization header (pre-send): {}", auth != null ? auth : "<absent>");
            }
        }
        @SuppressWarnings("unchecked")
        Publisher<HttpResponse<?>> proceed = (Publisher<HttpResponse<?>>) chain.proceed(request);
        return proceed;
    }
}

