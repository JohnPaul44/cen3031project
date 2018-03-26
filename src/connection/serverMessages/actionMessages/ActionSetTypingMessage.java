package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionSetTypingMessage extends ActionServerMessage{
    private String conversationKey;
    private boolean typing;

    public ActionSetTypingMessage(String conversationKey, boolean typing) {
        this.status = ServerMessage.Status.ACTIONSETTYPING.ordinal();
        this.conversationKey = conversationKey;
        this.typing = typing;
    }
}
