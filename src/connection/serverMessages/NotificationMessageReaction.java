package connection.serverMessages;

import model.Reactions;

import java.util.Map;

public class NotificationMessageReaction extends ServerMessage {
    private String conversationKey;
    private String messageKey;
    private Map<String, Reactions> reactions;

    public NotificationMessageReaction(String conversationKey, String messageKey, Map<String, Reactions> reactions) {
        this.status = Status.NOTIFICATIONMESSAGEREACTION;
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.reactions = reactions;
    }

    public String getConversationKey() {
        return conversationKey;
    }
    public String getMessageKey() {
        return messageKey;
    }
    public Map<String, Reactions> getReactions() {
        return reactions;
    }
}
