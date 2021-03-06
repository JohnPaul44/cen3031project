package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Conversation;

public class NotificationUserRemovedFromConversationMessage extends ServerMessage {
    private String username;
    private String conversationKey;
    private Conversation conversation;

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

    public Conversation getConversation() {
        return conversation;
    }
}
