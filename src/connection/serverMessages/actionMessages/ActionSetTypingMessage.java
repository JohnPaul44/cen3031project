package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;
import model.Message;

public class ActionSetTypingMessage extends ActionServerMessage{
    private Message message;
    private String conversationKey;
    private boolean typing;

    public ActionSetTypingMessage(Message message) {
        this.status = ServerMessage.Status.ACTIONSETTYPING.ordinal();
        this.message = message;
    }
}
