package ru.kara4un.ragdealer.agent.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidateIncomingMessage {
    private static final Logger LOG = LoggerFactory.getLogger(ValidateIncomingMessage.class);

    public String apply(String message) {
        LOG.debug("Action ValidateIncomingMessage: start (pre: message not empty)");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message must not be empty");
        }
        String normalized = message.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Message must contain non-whitespace characters");
        }
        LOG.debug("Action ValidateIncomingMessage: ok (post: normalized length={})", normalized.length());
        return normalized;
    }
}
