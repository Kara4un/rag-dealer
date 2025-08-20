package ru.kara4un.ragdealer.llm.gigachat;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GigaChatProperties.class)
public class GigaChatAutoConfiguration {
}
