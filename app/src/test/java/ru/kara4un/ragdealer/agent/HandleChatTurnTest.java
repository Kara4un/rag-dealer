package ru.kara4un.ragdealer.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Mono;
import ru.kara4un.ragdealer.agent.actions.CallLlm;
import ru.kara4un.ragdealer.agent.actions.ComposeLlmRequest;
import ru.kara4un.ragdealer.agent.actions.EnsureAccessToken;
import ru.kara4un.ragdealer.agent.actions.PersistTurn;
import ru.kara4un.ragdealer.agent.actions.PublishToClient;
import ru.kara4un.ragdealer.agent.actions.ValidateIncomingMessage;
import ru.kara4un.ragdealer.agent.goals.HandleChatTurn;
import ru.kara4un.ragdealer.chat.TokenManager;
import ru.kara4un.ragdealer.core.chat.InMemoryChatStore;

public class HandleChatTurnTest {

    @Test
    void executesHappyPath() {
        InMemoryChatStore store = new InMemoryChatStore();
        ValidateIncomingMessage validate = new ValidateIncomingMessage();
        TokenManager tm = mock(TokenManager.class);
        when(tm.getValidTokenReactive()).thenReturn(Mono.just("token"));
        EnsureAccessToken ensure = new EnsureAccessToken(tm);
        ComposeLlmRequest compose = new ComposeLlmRequest();
        ChatModel model = prompt -> new ChatResponse(java.util.List.of(new Generation(new AssistantMessage("hi"))));
        CallLlm call = new CallLlm(model);
        PersistTurn persist = new PersistTurn((conv, msg) -> {});
        PublishToClient publish = new PublishToClient();
        HandleChatTurn goal = new HandleChatTurn(store, validate, ensure, compose, call, persist, publish);
        ru.kara4un.ragdealer.chat.ChatResponse resp = goal.execute("c1", "hello");
        assertEquals("hi", resp.reply());
    }
}
