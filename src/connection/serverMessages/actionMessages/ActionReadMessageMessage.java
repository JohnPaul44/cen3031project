package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;
import model.Message;

public class ActionReadMessageMessage extends ActionServerMessage {
    private Message message;

    public ActionReadMessageMessage(String conversationKey) {
        this.status = ServerMessage.Status.ACTIONREADMESSAGE.ordinal();
        this.message = new Message(conversationKey);
    }
}
