package ru.kara4un.ragdealer.llm.gigachat;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.core.chat.ChatMessage;
import reactor.core.publisher.Mono;

@Component
public class GigaChatChatModel implements ChatModel {

    private final GigaChatClientAdapter adapter;

    public GigaChatChatModel(GigaChatClientAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        List<Message> messages = prompt.getInstructions();
        List<ChatMessage> history = new ArrayList<>();
        String userMessage = "";
        for (Message msg : messages) {
            if (msg instanceof UserMessage user) {
                userMessage = user.getText();
                history.add(new ChatMessage("user", user.getText(), null));
            }
        }
        String reply = adapter.chat(history, userMessage).block();
        AssistantMessage assistant = new AssistantMessage(reply);
        Generation generation = new Generation(assistant);
        return new ChatResponse(List.of(generation));
    }

    @Override
    public reactor.core.publisher.Flux<ChatResponse> stream(Prompt prompt) {
        return Mono.fromCallable(() -> call(prompt)).flux();
    }
}
