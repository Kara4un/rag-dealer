package ru.kara4un.ragdealer.agent.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class CallLlm {
    private static final Logger LOG = LoggerFactory.getLogger(CallLlm.class);

    private final ChatModel chatModel;

    public CallLlm(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String apply(Prompt prompt) {
        int size = prompt == null || prompt.getInstructions() == null ? 0 : prompt.getInstructions().size();
        LOG.debug("Action CallLlm: start (pre: promptMessages={})", size);
        ChatResponse response = chatModel.call(prompt);
        String text = response.getResult().getOutput().getText();
        LOG.debug(
                "Action CallLlm: ok (post: replyLength={} chars) sample='{}'",
                text == null ? 0 : text.length(),
                text == null ? "" : truncate(text, 120));
        return text;
    }

    private String truncate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "…";
    }
}
