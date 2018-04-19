package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Conversation;
import model.Message;

import java.util.Map;

public class NotificationMessageReceivedMessage extends ServerMessage {
    private Message message;
    private Map<String, Conversation> conversations;

    // Test constructor for new conversation
    public NotificationMessageReceivedMessage(Map<String, Conversation> conversation) {
        this.status = Status.NOTIFICATIONMESSAGERECEIVED.ordinal();
        this.conversations = conversation;
    }

    public NotificationMessageReceivedMessage(Message message) {
        this.status = Status.NOTIFICATIONMESSAGERECEIVED.ordinal();
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    public Map<String, Conversation> getConversation() { return conversations; }
}
