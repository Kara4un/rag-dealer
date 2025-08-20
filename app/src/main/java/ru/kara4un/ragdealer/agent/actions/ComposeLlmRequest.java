package ru.kara4un.ragdealer.agent.actions;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

@Component
public class ComposeLlmRequest {
    public Prompt apply(List<ChatMessage> history, String userMessage) {
        List<Message> messages = new ArrayList<>();
        for (ChatMessage msg : history) {
            if ("assistant".equals(msg.role())) {
                messages.add(new AssistantMessage(msg.content()));
            } else {
                messages.add(new UserMessage(msg.content()));
            }
        }
        messages.add(new UserMessage(userMessage));
        return new Prompt(messages);
    }
}
