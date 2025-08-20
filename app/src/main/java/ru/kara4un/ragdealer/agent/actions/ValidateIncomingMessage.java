package ru.kara4un.ragdealer.agent.actions;

import org.springframework.stereotype.Component;

@Component
public class ValidateIncomingMessage {
    public String apply(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be empty");
        }
        return message.trim();
    }
}
