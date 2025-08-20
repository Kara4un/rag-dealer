package ru.kara4un.ragdealer.agent.goals;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.agent.actions.CallLlm;
import ru.kara4un.ragdealer.agent.actions.ComposeLlmRequest;
import ru.kara4un.ragdealer.agent.actions.EnsureAccessToken;
import ru.kara4un.ragdealer.agent.actions.PersistTurn;
import ru.kara4un.ragdealer.agent.actions.PublishToClient;
import ru.kara4un.ragdealer.agent.actions.ValidateIncomingMessage;
import ru.kara4un.ragdealer.chat.ChatResponse;
import ru.kara4un.ragdealer.core.chat.ChatMessage;
import ru.kara4un.ragdealer.core.chat.InMemoryChatStore;
import org.springframework.ai.chat.prompt.Prompt;

@Component
public class HandleChatTurn {

    private final InMemoryChatStore chatStore;
    private final ValidateIncomingMessage validateIncomingMessage;
    private final EnsureAccessToken ensureAccessToken;
    private final ComposeLlmRequest composeLlmRequest;
    private final CallLlm callLlm;
    private final PersistTurn persistTurn;
    private final PublishToClient publishToClient;

    public HandleChatTurn(InMemoryChatStore chatStore,
                          ValidateIncomingMessage validateIncomingMessage,
                          EnsureAccessToken ensureAccessToken,
                          ComposeLlmRequest composeLlmRequest,
                          CallLlm callLlm,
                          PersistTurn persistTurn,
                          PublishToClient publishToClient) {
        this.chatStore = chatStore;
        this.validateIncomingMessage = validateIncomingMessage;
        this.ensureAccessToken = ensureAccessToken;
        this.composeLlmRequest = composeLlmRequest;
        this.callLlm = callLlm;
        this.persistTurn = persistTurn;
        this.publishToClient = publishToClient;
    }

    public ChatResponse execute(String conversationId, String message) {
        String normalized = validateIncomingMessage.apply(message);
        List<ChatMessage> history = chatStore.lastN(conversationId, 5);
        ensureAccessToken.apply();
        Prompt prompt = composeLlmRequest.apply(history, normalized);
        String reply = callLlm.apply(prompt);
        persistTurn.apply(conversationId, normalized, reply);
        chatStore.append(conversationId, new ChatMessage("user", normalized, Instant.now()));
        chatStore.append(conversationId, new ChatMessage("assistant", reply, Instant.now()));
        List<ChatMessage> updatedHistory = chatStore.lastN(conversationId, 5);
        return publishToClient.apply(reply, updatedHistory);
    }
}
