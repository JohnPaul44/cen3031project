package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;

public class NotificationMessageReadMessage extends ServerMessage {
    private String conversationKey;
    private String from;

    public NotificationMessageReadMessage(String conversationKey, String from) {
        this.status = Status.NOTIFICATIONMESSAGEREAD.ordinal();
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
