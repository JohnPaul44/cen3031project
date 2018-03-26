package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Conversation;
import model.Reactions;

import java.util.Map;

public class NotificationMessageReceivedMessage extends ServerMessage {
    private String conversationKey;
    private String messageKey;
    private String serverTime;
    private String from;
    private String text;
    private Map<String, Reactions> reactions;
    private Conversation conversation;

    // Test constructor for existing conversation
    public NotificationMessageReceivedMessage(String conversationKey, String messageKey, String serverTime, String from, String text, Map<String, Reactions> reactions) {
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
    public Map<String, Reactions> getReactions() {
        return reactions;
    }
    public Conversation getConversation() { return conversation; }
}
