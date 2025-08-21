package ru.kara4un.ragdealer.core.embeddings;

public class EmbeddingsException extends RuntimeException {
    public EmbeddingsException(String message) {
        super(message);
    }

    public EmbeddingsException(String message, Throwable cause) {
        super(message, cause);
    }
}
