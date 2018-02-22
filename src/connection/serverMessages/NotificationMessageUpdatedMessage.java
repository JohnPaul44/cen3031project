package connection.serverMessages;

public class NotificationMessageUpdatedMessage extends ServerMessage {
    private String conversationKey;
    private String messageKey;
    private String text;

    public NotificationMessageUpdatedMessage(String conversationKey, String messageKey, String text) {
        this.status = Status.NOTIFICATIONMESSAGEUPDATED.ordinal();
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.text = text;
    }

    public String getConversationKey() {
        return conversationKey;
    }
    public String getMessageKey() {
        return messageKey;
    }
    public String getText() {
        return text;
    }
}