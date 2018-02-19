package connection.serverMessages;

import model.Conversation;
import model.UserReaction;

public class NotificationMessageReceivedMessage extends ServerMessage {
    private String conversationKey;
    private String messageKey;
    private String serverTime;
    private String from;
    private String text;
    private UserReaction[] reactions;
    private Conversation conversation;

    // Test constructor for existing conversation
    public NotificationMessageReceivedMessage(String conversationKey, String messageKey, String serverTime, String from, String text, UserReaction[] reactions) {
        this.status = Status.NOTIFICATIONMESSAGERECEIVED.ordinal();
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.serverTime = serverTime;
        this.from = from;
        this.text = text;
        this.reactions = reactions;
    }
    // Test constructor for new conversation
    public NotificationMessageReceivedMessage(Conversation conversation) {
        this.status = Status.NOTIFICATIONMESSAGERECEIVED.ordinal();
        this.conversation = conversation;
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
    public Conversation getConversation() { return conversation; }
}
