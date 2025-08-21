package ru.kara4un.ragdealer.agent.actions;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class ComposeLlmRequest {
    private static final Logger LOG = LoggerFactory.getLogger(ComposeLlmRequest.class);

    public Prompt apply(List<ChatMessage> history, String userMessage) {
        LOG.debug(
                "Action ComposeLlmRequest: start (pre: historySize={}, userTextLength={})",
                history == null ? 0 : history.size(),
                userMessage == null ? 0 : userMessage.length());
        List<Message> messages = new ArrayList<>();
        for (ChatMessage msg : history) {
            if ("assistant".equals(msg.role())) {
                messages.add(new AssistantMessage(msg.content()));
            } else {
                messages.add(new UserMessage(msg.content()));
            }
        }
        messages.add(new UserMessage(userMessage));
        Prompt prompt = new Prompt(messages);
        LOG.debug("Action ComposeLlmRequest: ok (post: promptMessages={})", messages.size());
        return prompt;
    }
}
