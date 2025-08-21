package ru.kara4un.ragdealer.core.embeddings;

import java.util.List;

public interface EmbeddingsClient {
    /**
     * Возвращает эмбеддинги для каждого входного текста.
     * Размер результирующего списка равен размеру входного.
     */
    List<float[]> embed(List<String> texts);
}
