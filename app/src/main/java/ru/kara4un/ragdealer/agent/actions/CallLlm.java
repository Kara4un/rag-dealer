package ru.kara4un.ragdealer.agent.actions;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class CallLlm {
    private final ChatModel chatModel;

    public CallLlm(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String apply(Prompt prompt) {
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
