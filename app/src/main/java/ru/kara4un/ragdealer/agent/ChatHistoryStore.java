package ru.kara4un.ragdealer.agent;

import ru.kara4un.ragdealer.core.chat.ChatMessage;

public interface ChatHistoryStore {
    void append(String conversationId, ChatMessage message);
}
