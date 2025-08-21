package ru.kara4un.ragdealer.agent.goals;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(HandleChatTurn.class);

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
        LOG.info("Goal HandleChatTurn: start conversationId={}", conversationId);
        LOG.info("Plan:\n{}", ru.kara4un.ragdealer.agent.logging.PlanLogger.handleChatTurnPlan());
        String normalized = validateIncomingMessage.apply(message);
        List<ChatMessage> history = chatStore.lastN(conversationId, 5);
        ensureAccessToken.apply();
        Prompt prompt = composeLlmRequest.apply(history, normalized);
        String reply = callLlm.apply(prompt);
        persistTurn.apply(conversationId, normalized, reply);
        LOG.debug("AppendToChatStore[user]: conversationId={} length={} chars", conversationId, normalized.length());
        chatStore.append(conversationId, new ChatMessage("user", normalized, Instant.now()));
        LOG.debug("AppendToChatStore[assistant]: conversationId={} length={} chars", conversationId, reply.length());
        chatStore.append(conversationId, new ChatMessage("assistant", reply, Instant.now()));
        List<ChatMessage> updatedHistory = chatStore.lastN(conversationId, 5);
        ChatResponse response = publishToClient.apply(reply, updatedHistory);
        LOG.info("Goal HandleChatTurn: done conversationId={} historySize={}", conversationId, updatedHistory.size());
        return response;
    }
}
