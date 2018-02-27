package connection.serverMessages;

public class NotificationMessageReadMessage extends ServerMessage {
    private String conversationKey;
    private String from;

    public NotificationMessageReadMessage(String conversationKey, String from) {
        this.status = Status.NOTIFICATIONMESSAGEREAD;
        this.conversationKey = conversationKey;
        this.from = from;
    }

    public String getConversationKey() {
        return conversationKey;
    }
    public String getFrom() {
        return from;
    }
}
