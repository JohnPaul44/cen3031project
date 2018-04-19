package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Message;

public class NotificationTypingMessage extends ServerMessage {
    private Message message;

    public NotificationTypingMessage(Message message) {
        this.status = Status.NOTIFICATIONTYPING.ordinal();
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
