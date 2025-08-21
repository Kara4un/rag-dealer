package ru.kara4un.ragdealer.agent.logging;

/**
 * Utility to render readable execution plans for goals.
 */
public final class PlanLogger {

    private PlanLogger() {}

    public static String handleChatTurnPlan() {
        // ASCII tree describing the execution plan for the HandleChatTurn goal
        return String.join("\n",
                "HandleChatTurn",
                "├─ ValidateIncomingMessage  (pre: message not empty; post: normalized not empty)",
                "├─ EnsureAccessToken       (pre: token may be missing/expired; post: valid token available)",
                "├─ ComposeLlmRequest       (pre: history collected; post: Prompt built)",
                "├─ CallLlm                 (pre: Prompt ready; post: reply text generated)",
                "├─ PersistTurn             (pre: reply available; post: persisted to history store)",
                "├─ AppendToChatStore[user] (pre: normalized user text; post: appended to in-memory store)",
                "├─ AppendToChatStore[assistant] (pre: reply text; post: appended to in-memory store)",
                "└─ PublishToClient         (pre: reply + recent history; post: response sent)");
    }
}

