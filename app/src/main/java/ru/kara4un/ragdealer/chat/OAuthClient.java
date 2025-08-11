package ru.kara4un.ragdealer.chat;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.HttpHeaders;
import com.fasterxml.jackson.annotation.JsonProperty;

@Client("${gigachat.oauth.base-url}")
public interface OAuthClient {

    @Post(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    TokenResponse token(@Header(HttpHeaders.AUTHORIZATION) String authorization, @Body String body);

    record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("expires_at") String expiresAt) {}
}
