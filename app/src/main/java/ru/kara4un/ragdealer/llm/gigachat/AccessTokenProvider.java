package ru.kara4un.ragdealer.llm.gigachat;

@FunctionalInterface
public interface AccessTokenProvider {
    String getAccessToken();
}
