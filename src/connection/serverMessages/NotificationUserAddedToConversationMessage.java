package connection.serverMessages;

import model.Conversation;

public class NotificationUserAddedToConversationMessage extends ServerMessage {
    private String username;
    private String conversationKey;
    private Conversation conversation;

    public NotificationUserAddedToConversationMessage(String username, String conversationKey) {
        this.status = Status.NOTIFICATIONUSERADDEDTOCONVERSATION;
        this.username = username;
        this.conversationKey = conversationKey;
    }

    public NotificationUserAddedToConversationMessage(Conversation conversation) {
        this.status = Status.NOTIFICATIONUSERADDEDTOCONVERSATION;
        this.conversation = conversation;
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
