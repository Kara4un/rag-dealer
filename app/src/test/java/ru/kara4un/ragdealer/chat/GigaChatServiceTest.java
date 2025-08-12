package ru.kara4un.ragdealer.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.kara4un.ragdealer.core.chat.ChatMessage;
import ru.kara4un.ragdealer.gigachat.api.DefaultApi;
import ru.kara4un.ragdealer.gigachat.model.ChatCompletion;
import ru.kara4un.ragdealer.gigachat.model.Choices;
import ru.kara4un.ragdealer.gigachat.model.MessagesRes;

class GigaChatServiceTest {

    @Test
    void generateReply_returnsAssistantMessageContent() throws Exception {
        TokenManager tokenManager = mock(TokenManager.class);
        when(tokenManager.getValidTokenReactive()).thenReturn(Mono.just("dummy-token"));

        GigaChatService service = new GigaChatService(tokenManager, "test-model");
        // trigger ApiClient init (not strictly needed for this test)
        service.init();

        // Mock DefaultApi and inject into service via reflection
        DefaultApi defaultApi = mock(DefaultApi.class);
        MessagesRes msg = new MessagesRes().role(MessagesRes.RoleEnum.ASSISTANT).content("hello");
        Choices choice = new Choices().message(msg);
        ChatCompletion completion = new ChatCompletion().choices(List.of(choice));
        when(defaultApi.postChat(any(), any(), any(), any())).thenReturn(completion);

        Field f = GigaChatService.class.getDeclaredField("defaultApi");
        f.setAccessible(true);
        f.set(service, defaultApi);

        List<ChatMessage> history = List.of(
                new ChatMessage("user", "hi", Instant.now()),
                new ChatMessage("assistant", "hey", Instant.now())
        );

        StepVerifier.create(service.generateReply(history, "How are you?"))
                .expectNext("hello")
                .verifyComplete();
    }
}
