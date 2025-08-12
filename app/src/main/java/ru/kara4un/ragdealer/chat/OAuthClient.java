package ru.kara4un.ragdealer.chat;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.annotation.Client;
import java.util.Map;
import org.reactivestreams.Publisher;

// Bind to named service to control error handling and capture bodies
@Client(id = "ngw", value = "${gigachat.oauth.base-url}")
public interface OAuthClient {

    @Post(value = "/oauth", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.ALL)
    Publisher<HttpResponse<String>> token(
            @Header(HttpHeaders.AUTHORIZATION) String authorization,
            @Header(HttpHeaders.CONTENT_TYPE) String contentType,
            @Header(HttpHeaders.ACCEPT) String accept,
            @Header("RqUID") String rqUid,
            @Body Map<String, String> form);
}
