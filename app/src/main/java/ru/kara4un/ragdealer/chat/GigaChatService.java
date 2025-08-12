package ru.kara4un.ragdealer.chat;

import io.micronaut.context.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import ru.kara4un.ragdealer.core.chat.ChatMessage;
import ru.kara4un.ragdealer.gigachat.api.DefaultApi;
import ru.kara4un.ragdealer.gigachat.invoker.ApiClient;
import ru.kara4un.ragdealer.gigachat.invoker.ApiException;
import ru.kara4un.ragdealer.gigachat.model.Chat;
import ru.kara4un.ragdealer.gigachat.model.ChatCompletion;
import ru.kara4un.ragdealer.gigachat.model.Message;
import okhttp3.OkHttpClient;

@Singleton
public class GigaChatService {

    private final TokenManager tokenManager;
    private final String modelId;
    private DefaultApi defaultApi;
    private final String apiBaseUrl;
    private final boolean insecureTrustAll;

    public GigaChatService(TokenManager tokenManager,
                           @Value("${gigachat.api.model-id}") String modelId,
                           @Value("${gigachat.api.base-url}") String apiBaseUrl,
                           @Value("${micronaut.http.client.ssl.insecure-trust-all-certificates:false}") boolean insecureTrustAll) {
        this.tokenManager = tokenManager;
        this.modelId = modelId;
        this.apiBaseUrl = apiBaseUrl;
        this.insecureTrustAll = insecureTrustAll;
    }

    // Backward-compatible constructor for tests and simple wiring
    public GigaChatService(TokenManager tokenManager, String modelId) {
        this(tokenManager, modelId, "https://gigachat.devices.sberbank.ru/api/v1", false);
    }

    @PostConstruct
    void init() {
        ApiClient apiClient = new ApiClient();
        if (apiBaseUrl != null && !apiBaseUrl.isBlank()) {
            apiClient.setBasePath(apiBaseUrl);
        }

        if (insecureTrustAll) {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                            @Override
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                            @Override
                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        }
                };
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient.Builder builder = new OkHttpClient.Builder()
                        .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                        .hostnameVerifier((hostname, session) -> true);
                apiClient.setHttpClient(builder.build());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to configure insecure SSL for OpenAPI client", e);
            }
        }
        // Non-blocking supplier: uses cached token; ensure validity in reactive chain before requests
        apiClient.setBearerToken(() -> tokenManager.getCachedToken());
        this.defaultApi = new DefaultApi(apiClient);
    }

    public Publisher<String> generateReply(List<ChatMessage> history, String userMessage) {
        Chat chat = new Chat();
        chat.setModel(modelId);
        List<Message> messages = new ArrayList<>();
        for (ChatMessage msg : history) {
            Message m = new Message().role(Message.RoleEnum.fromValue(msg.role())).content(msg.content());
            messages.add(m);
        }
        messages.add(new Message().role(Message.RoleEnum.USER).content(userMessage));
        chat.setMessages(messages);

        return Mono.defer(() -> tokenManager.getValidTokenReactive())
                .then(Mono.fromCallable(() -> defaultApi.postChat(null, null, null, chat))
                        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()))
                .map(ChatCompletion::getChoices)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).getMessage().getContent());
    }
}
