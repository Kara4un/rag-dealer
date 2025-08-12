package ru.kara4un.ragdealer.chat;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.cookie.Cookie;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import ru.kara4un.ragdealer.core.chat.ChatMessage;
import ru.kara4un.ragdealer.core.chat.InMemoryChatStore;

@Controller("/api")
public class ChatController {

    private final InMemoryChatStore chatStore;
    private final GigaChatService chatService;

    public ChatController(InMemoryChatStore chatStore, GigaChatService chatService) {
        this.chatStore = chatStore;
        this.chatService = chatService;
    }

    @Post(value = "/chat", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public Publisher<HttpResponse<ChatResponse>> chat(@Body ChatRequest request, HttpRequest<?> httpRequest) {
        Optional<Cookie> cookieOpt = httpRequest.getCookies().findCookie("SESSION_ID");
        boolean newSession = cookieOpt.isEmpty();
        String sessionId = cookieOpt.map(Cookie::getValue).orElse(UUID.randomUUID().toString());
        List<ChatMessage> history = chatStore.lastN(sessionId, 5);
        return Mono.from(chatService.generateReply(history, request.text()))
                .map(reply -> {
                    chatStore.append(sessionId, new ChatMessage("user", request.text(), Instant.now()));
                    chatStore.append(sessionId, new ChatMessage("assistant", reply, Instant.now()));
                    ChatResponse body = new ChatResponse(reply, chatStore.lastN(sessionId, 5));
                    MutableHttpResponse<ChatResponse> response = HttpResponse.ok(body);
                    if (newSession) {
                        response.cookie(Cookie.of("SESSION_ID", sessionId));
                    }
                    return response;
                });
    }

    @Get("/history")
    public HttpResponse<List<ChatMessage>> history(HttpRequest<?> httpRequest) {
        Optional<Cookie> cookieOpt = httpRequest.getCookies().findCookie("SESSION_ID");
        boolean newSession = cookieOpt.isEmpty();
        String sessionId = cookieOpt.map(Cookie::getValue).orElse(UUID.randomUUID().toString());
        List<ChatMessage> history = chatStore.lastN(sessionId, 5);
        MutableHttpResponse<List<ChatMessage>> response = HttpResponse.ok(history);
        if (newSession) {
            response.cookie(Cookie.of("SESSION_ID", sessionId));
        }
        return response;
    }
}
