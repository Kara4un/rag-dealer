package ru.kara4un.ragdealer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class HelloControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void helloEndpointReturnsGreeting() {
        String response = client.toBlocking().retrieve(HttpRequest.GET("/"));
        assertEquals("Hello, World!", response);
    }
}
