package ru.kara4un.ragdealer.chat;

import java.util.List;
import ru.kara4un.ragdealer.core.chat.ChatMessage;

public record ChatResponse(String reply, List<ChatMessage> history) {}
