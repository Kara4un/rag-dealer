package ru.kara4un.ragdealer.core.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryChatStoreTest {

    @Test
    void returnsLastFiveMessages() {
        InMemoryChatStore store = new InMemoryChatStore();
        String session = "s1";
        for (int i = 1; i <= 10; i++) {
            store.append(session, new ChatMessage("user", "m" + i, Instant.ofEpochSecond(i)));
        }
        List<ChatMessage> last = store.lastN(session, 5);
        assertEquals(5, last.size());
        assertEquals("m6", last.get(0).content());
        assertEquals("m10", last.get(4).content());
    }
}
