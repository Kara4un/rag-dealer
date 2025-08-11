package ru.kara4un.ragdealer.core.chat;

import java.time.Instant;

public record ChatMessage(String role, String content, Instant timestamp) {
}
