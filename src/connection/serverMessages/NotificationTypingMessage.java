package connection.serverMessages;

public class NotificationTypingMessage extends ServerMessage {
    private String conversationKey;
    private String from;
    private boolean typing;

    public NotificationTypingMessage(String conversationKey, String from, boolean typing) {
        this.status = Status.NOTIFICATIONTYPING.ordinal();
        this.conversationKey = conversationKey;
        this.from = from;
        this.typing = typing;
    }

    public String getConversationKey() {
        return conversationKey;
    }
    public String getFrom() {
        return from;
    }
    public boolean getTyping() {
        return typing;
    }
}
