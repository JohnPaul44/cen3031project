package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;
import model.Reactions;

import java.util.Map;

public class ActionReactToMessage extends ActionServerMessage{
    private String conversationKey;
    private String messageKey;
    private Map<String, Reactions> reactions;

    public ActionReactToMessage(String conversationKey, String messageKey, Map<String, Reactions> reactions) {
        this.status = ServerMessage.Status.ACTIONREACTTOMESSAGE.ordinal();
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.reactions = reactions;
    }
}
