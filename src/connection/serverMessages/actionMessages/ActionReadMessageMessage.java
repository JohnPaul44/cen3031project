package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionReadMessageMessage extends ActionServerMessage {
    private String conversationKey;

    public ActionReadMessageMessage(String conversationKey) {
        this.status = ServerMessage.Status.ACTIONREADMESSAGE.ordinal();
        this.conversationKey = conversationKey;
    }
}
