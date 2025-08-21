package ru.kara4un.ragdealer.agent.actions;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.chat.ChatResponse;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class PublishToClient {
    private static final Logger LOG = LoggerFactory.getLogger(PublishToClient.class);

    public ChatResponse apply(String reply, List<ChatMessage> history) {
        LOG.debug(
                "Action PublishToClient: start (pre: replyLen={}, historySize={})",
                reply == null ? 0 : reply.length(),
                history == null ? 0 : history.size());
        ChatResponse response = new ChatResponse(reply, history);
        LOG.debug("Action PublishToClient: ok (post: response ready)");
        return response;
    }
}
