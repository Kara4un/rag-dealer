package ru.kara4un.ragdealer.agent.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.kara4un.ragdealer.chat.TokenManager;

@Component
public class EnsureAccessToken {
    private static final Logger LOG = LoggerFactory.getLogger(EnsureAccessToken.class);

    private final TokenManager tokenManager;

    public EnsureAccessToken(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public void apply() {
        LOG.debug("Action EnsureAccessToken: start (pre: token may be missing/expired)");
        tokenManager.getValidTokenReactive().block();
        LOG.debug("Action EnsureAccessToken: ok (post: valid token available)");
    }
}
