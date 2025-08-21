package ru.kara4un.ragdealer.embeddings;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.kara4un.ragdealer.gigachat.api.DefaultApi;
import ru.kara4un.ragdealer.gigachat.invoker.ApiClient;

public class GigaChatEmbeddingsClientTest {

    private MockWebServer server;
    private GigaChatEmbeddingsClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(server.url("/").toString());
        DefaultApi api = new DefaultApi(apiClient);
        client = new GigaChatEmbeddingsClient(api, "Embeddings", 2, 3, Duration.ofSeconds(1));
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void embedsInBatches() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{" +
                        "\"data\":[{" +
                        "\"embedding\":[0.1,0.2],\"index\":0},{" +
                        "\"embedding\":[0.3,0.4],\"index\":1}]," +
                        "\"model\":\"Embeddings\",\"object\":\"list\"}"));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{" +
                        "\"data\":[{" +
                        "\"embedding\":[0.5,0.6],\"index\":0}]," +
                        "\"model\":\"Embeddings\",\"object\":\"list\"}"));
        List<float[]> vectors = client.embed(List.of("a", "b", "c"));
        assertEquals(3, vectors.size());
        assertArrayEquals(new float[]{0.1f, 0.2f}, vectors.get(0));
        assertArrayEquals(new float[]{0.3f, 0.4f}, vectors.get(1));
        assertArrayEquals(new float[]{0.5f, 0.6f}, vectors.get(2));
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void retriesOnTemporaryErrors() {
        server.enqueue(new MockResponse().setResponseCode(500));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{" +
                        "\"data\":[{" +
                        "\"embedding\":[1.0],\"index\":0}]," +
                        "\"model\":\"Embeddings\",\"object\":\"list\"}"));
        List<float[]> vectors = client.embed(List.of("t"));
        assertEquals(1, vectors.size());
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void parsesFloatVectors() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{" +
                        "\"data\":[{" +
                        "\"embedding\":[1.0,2.5],\"index\":0}]," +
                        "\"model\":\"Embeddings\",\"object\":\"list\"}"));
        List<float[]> vectors = client.embed(List.of("x"));
        assertArrayEquals(new float[]{1.0f, 2.5f}, vectors.get(0));
    }
}
