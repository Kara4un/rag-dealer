package ru.kara4un.ragdealer.embeddings;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ru.kara4un.ragdealer.core.embeddings.EmbeddingsClient;
import ru.kara4un.ragdealer.core.embeddings.EmbeddingsException;
import ru.kara4un.ragdealer.gigachat.api.DefaultApi;
import ru.kara4un.ragdealer.gigachat.invoker.ApiException;
import ru.kara4un.ragdealer.gigachat.model.Embedding;
import ru.kara4un.ragdealer.gigachat.model.EmbeddingDataInner;
import ru.kara4un.ragdealer.gigachat.model.EmbeddingsBody;

public class GigaChatEmbeddingsClient implements EmbeddingsClient {

    private final DefaultApi api;
    private final String model;
    private final int batchMaxSize;
    private final int maxAttempts;
    private final Duration timeout;

    public GigaChatEmbeddingsClient(
            DefaultApi api,
            String model,
            int batchMaxSize,
            int maxAttempts,
            Duration timeout
    ) {
        this.api = api;
        this.model = model;
        this.batchMaxSize = batchMaxSize;
        this.maxAttempts = maxAttempts;
        this.timeout = timeout;
        int ms = (int) timeout.toMillis();
        api.getApiClient().setReadTimeout(ms);
        api.getApiClient().setWriteTimeout(ms);
        api.getApiClient().setConnectTimeout(ms);
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<float[]> result = new ArrayList<>(Collections.nCopies(texts.size(), null));
        int base = 0;
        for (List<String> batch : chunk(texts, batchMaxSize)) {
            EmbeddingsBody req = new EmbeddingsBody()
                    .model(model)
                    .input(batch);
            Embedding resp = callWithRetry(() -> api.postEmbeddings(req));
            for (EmbeddingDataInner d : resp.getData()) {
                List<Float> vec = d.getEmbedding();
                float[] fv = new float[vec.size()];
                for (int i = 0; i < vec.size(); i++) {
                    fv[i] = vec.get(i);
                }
                int targetIndex = base + d.getIndex();
                result.set(targetIndex, fv);
            }
            base += batch.size();
        }
        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) == null) {
                throw new EmbeddingsException("Missing embedding at index " + i);
            }
        }
        return result;
    }

    private static <T> List<List<T>> chunk(List<T> src, int size) {
        List<List<T>> out = new ArrayList<>();
        for (int i = 0; i < src.size(); i += size) {
            out.add(src.subList(i, Math.min(i + size, src.size())));
        }
        return out;
    }

    private <T> T callWithRetry(SupplierWithException<T> supplier) {
        long backoffMs = 200L;
        Throwable last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (Throwable t) {
                last = t;
                if (attempt < maxAttempts && isRetriable(t)) {
                    sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, 5_000L);
                    continue;
                }
                break;
            }
        }
        throw new EmbeddingsException("Embeddings API failed after " + maxAttempts + " attempts", last);
    }

    private boolean isRetriable(Throwable t) {
        if (t instanceof ApiException ae) {
            int code = ae.getCode();
            return code == 429 || (code >= 500 && code < 600);
        }
        return t instanceof java.io.IOException || (t.getCause() instanceof java.io.IOException);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> { T get() throws Exception; }
}
