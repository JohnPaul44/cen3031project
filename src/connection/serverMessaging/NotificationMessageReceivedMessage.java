package connection.serverMessaging;

import model.UserReaction;

public class NotificationMessageReceivedMessage extends ServerMessage {
    private String conversationKey;
    private String messageKey;
    private String serverTime;
    private String from;
    private String text;
    private UserReaction[] reactions;

    public NotificationMessageReceivedMessage(String conversationKey, String messageKey, String serverTime, String from, String text, UserReaction[] reactions) {
        this.status = Status.NOTIFICATIONMESSAGERECEIVED.ordinal();
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.serverTime = serverTime;
        this.from = from;
        this.text = text;
        this.reactions = reactions;
    }

    public NotificationMessageReceivedMessage(String conversationKey, String messageKey, String serverTime, String from, String text) {
        this.status = Status.NOTIFICATIONMESSAGERECEIVED.ordinal();
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.serverTime = serverTime;
        this.from = from;
        this.text = text;
    }

    public String getConversationKey() {
        return conversationKey;
    }
    public String getMessageKey() {
        return messageKey;
    }
    public String getServerTime() {
        return serverTime;
    }
    public String getFrom() {
        return from;
    }
    public String getText() {
        return text;
    }
    public UserReaction[] getReactions() {
        return reactions;
    }
}
