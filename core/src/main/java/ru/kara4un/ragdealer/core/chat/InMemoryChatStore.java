package ru.kara4un.ragdealer.core.chat;

import jakarta.inject.Singleton;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class InMemoryChatStore {
    private final Map<String, Deque<ChatMessage>> store = new ConcurrentHashMap<>();

    public void append(String sessionId, ChatMessage message) {
        store.computeIfAbsent(sessionId, id -> new ArrayDeque<>()).addLast(message);
    }

    public List<ChatMessage> lastN(String sessionId, int n) {
        Deque<ChatMessage> deque = store.get(sessionId);
        if (deque == null || deque.isEmpty()) {
            return List.of();
        }
        int size = deque.size();
        int skip = Math.max(0, size - n);
        List<ChatMessage> result = new ArrayList<>();
        int index = 0;
        for (ChatMessage message : deque) {
            if (index++ >= skip) {
                result.add(message);
            }
        }
        return result;
    }

    public void clear(String sessionId) {
        store.remove(sessionId);
    }
}
