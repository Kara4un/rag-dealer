package ru.kara4un.ragdealer.agent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import ru.kara4un.ragdealer.agent.actions.EnsureAccessToken;
import ru.kara4un.ragdealer.chat.TokenManager;

public class EnsureAccessTokenTest {

    @Test
    void refreshesTokenWhenCalled() {
        TokenManager tm = mock(TokenManager.class);
        when(tm.getValidTokenReactive()).thenReturn(Mono.just("token"));
        EnsureAccessToken ensure = new EnsureAccessToken(tm);
        ensure.apply();
        verify(tm, times(1)).getValidTokenReactive();
    }
}
