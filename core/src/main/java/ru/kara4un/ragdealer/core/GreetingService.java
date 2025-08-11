package ru.kara4un.ragdealer.core;

import jakarta.inject.Singleton;

@Singleton
public class GreetingService {
    public String greeting() {
        return "Hello, World!";
    }
}
