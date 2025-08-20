package ru.kara4un.ragdealer.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Temporary helper to allow connecting to HTTPS endpoints with untrusted/invalid certificates.
 * Controlled via property: http.client.ssl.insecure-trust-all-certificates
 */
@Configuration
public class InsecureSslWebClientConfig {
    private static final Logger log = LoggerFactory.getLogger(InsecureSslWebClientConfig.class);

    @Bean
    public WebClientCustomizer insecureSslCustomizer(
            @Value("${http.client.ssl.insecure-trust-all-certificates:false}") boolean insecure) {
        return (WebClient.Builder builder) -> {
            if (!insecure) {
                return;
            }
            try {
                SslContext sslContext = SslContextBuilder
                        .forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                HttpClient httpClient = HttpClient.create()
                        .secure(ssl -> ssl.sslContext(sslContext));

                builder.clientConnector(new ReactorClientHttpConnector(httpClient));
                log.warn("Insecure SSL is ENABLED: trusting all server certificates for WebClient. Use only in dev/test!");
            } catch (Exception e) {
                log.error("Failed to configure insecure SSL for WebClient", e);
            }
        };
    }
}
