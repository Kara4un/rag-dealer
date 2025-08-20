package ru.kara4un.ragdealer.llm.gigachat;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.ai.gigachat")
public class GigaChatProperties {

    /** Base URL of the GigaChat API. */
    private String baseUrl;

    /** Model identifier. */
    private String model;

    /** Request timeout (in seconds). */
    private java.time.Duration timeout = java.time.Duration.ofSeconds(30);

    private double temperature = 0.7d;

    private double topP = 0.95d;

    private int maxTokens = 512;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public java.time.Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(java.time.Duration timeout) {
        this.timeout = timeout;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getTopP() {
        return topP;
    }

    public void setTopP(double topP) {
        this.topP = topP;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
}
