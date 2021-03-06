package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Message;

public class NotificationMessageReadMessage extends ServerMessage {
    private Message message;

//    public NotificationMessageReadMessage(String conversationKey, String from) {
//        this.status = Status.NOTIFICATIONMESSAGEREAD.ordinal();
//        this.conversationKey = conversationKey;
//        this.from = from;
//    }

    public String getConversationKey() { return message.getConversationKey(); }

    public String getFrom() {
        return message.getFrom();
    }

    public Message getMessage() {
        return message;
    }
}
