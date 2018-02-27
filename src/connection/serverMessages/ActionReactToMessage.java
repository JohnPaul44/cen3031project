package connection.serverMessages;

import model.Reactions;

import java.util.Map;

public class ActionReactToMessage extends ActionServerMessage{
    private String conversationKey;
    private String messageKey;
    private Map<String, Reactions> reactions;

    public ActionReactToMessage(String conversationKey, String messageKey, Map<String, Reactions> reactions) {
        this.status = Status.ACTIONREACTTOMESSAGE;
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.reactions = reactions;
    }
}
