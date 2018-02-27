package connection.serverMessages;

public class NotificationUserRemovedFromConversationMessage extends ServerMessage {
    private String username;
    private String conversationKey;

    public NotificationUserRemovedFromConversationMessage(String username, String conversationKey) {
        this.status = Status.NOTIFICATIONUSERREMOVEDFROMCONVERSATION.ordinal();
        this.username = username;
        this.conversationKey = conversationKey;
    }

    public String getUsername() {
        return username;
    }
    public String getConversationKey() {
        return conversationKey;
    }
}
