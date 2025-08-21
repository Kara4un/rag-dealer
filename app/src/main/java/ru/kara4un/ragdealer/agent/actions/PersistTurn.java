package ru.kara4un.ragdealer.agent.actions;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.agent.ChatHistoryStore;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class PersistTurn {
    private static final Logger LOG = LoggerFactory.getLogger(PersistTurn.class);

    private final ChatHistoryStore store;

    public PersistTurn(ChatHistoryStore store) {
        this.store = store;
    }

    public void apply(String conversationId, String userMessage, String assistantMessage) {
        LOG.debug(
                "Action PersistTurn: start (pre: convId={}, userLen={}, replyLen={})",
                conversationId,
                userMessage == null ? 0 : userMessage.length(),
                assistantMessage == null ? 0 : assistantMessage.length());
        store.append(conversationId, new ChatMessage("user", userMessage, Instant.now()));
        store.append(conversationId, new ChatMessage("assistant", assistantMessage, Instant.now()));
        LOG.debug("Action PersistTurn: ok (post: two records persisted)");
    }
}
