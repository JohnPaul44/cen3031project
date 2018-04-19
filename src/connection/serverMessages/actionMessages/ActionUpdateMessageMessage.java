package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionUpdateMessageMessage extends ActionServerMessage{
    private String conversationKey;
    private String messageKey;
    private String text;

    public ActionUpdateMessageMessage(String conversationKey, String messageKey, String text) {
        this.status = ServerMessage.Status.ACTIONUPDATEMESSAGE.ordinal();
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.text = text;
    }
}
