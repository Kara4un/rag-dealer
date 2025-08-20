package ru.kara4un.ragdealer.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class OpenSearchChatHistoryStore implements ChatHistoryStore {

    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchChatHistoryStore.class);

    @Override
    public void append(String conversationId, ChatMessage message) {
        // In a real implementation this would index the message into OpenSearch.
        LOG.debug("append conversation={} role={} content={}", conversationId, message.role(), message.content());
    }
}
