package ru.kara4un.ragdealer.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kara4un.ragdealer.core.chat.ChatMessage;
import ru.kara4un.ragdealer.core.chat.InMemoryChatStore;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final InMemoryChatStore chatStore;
    private final GigaChatService chatService;

    public ChatController(InMemoryChatStore chatStore, GigaChatService chatService) {
        this.chatStore = chatStore;
        this.chatService = chatService;
    }

    @PostMapping(value = "/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request,
                                             @CookieValue(value = "SESSION_ID", required = false) String sessionIdCookie) {
        boolean newSession = sessionIdCookie == null;
        String sessionId = newSession ? UUID.randomUUID().toString() : sessionIdCookie;
        List<ChatMessage> history = chatStore.lastN(sessionId, 5);
        String reply = chatService.generateReply(history, request.text()).block();
        chatStore.append(sessionId, new ChatMessage("user", request.text(), Instant.now()));
        chatStore.append(sessionId, new ChatMessage("assistant", reply, Instant.now()));
        ChatResponse body = new ChatResponse(reply, chatStore.lastN(sessionId, 5));
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (newSession) {
            ResponseCookie cookie = ResponseCookie.from("SESSION_ID", sessionId).path("/").build();
            builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return builder.body(body);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> history(@CookieValue(value = "SESSION_ID", required = false) String sessionIdCookie) {
        boolean newSession = sessionIdCookie == null;
        String sessionId = newSession ? UUID.randomUUID().toString() : sessionIdCookie;
        List<ChatMessage> history = chatStore.lastN(sessionId, 5);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (newSession) {
            ResponseCookie cookie = ResponseCookie.from("SESSION_ID", sessionId).path("/").build();
            builder.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return builder.body(history);
    }
}
