package ru.kara4un.ragdealer.agent.actions;

import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.chat.TokenManager;

@Component
public class EnsureAccessToken {
    private final TokenManager tokenManager;

    public EnsureAccessToken(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public void apply() {
        tokenManager.getValidTokenReactive().block();
    }
}
