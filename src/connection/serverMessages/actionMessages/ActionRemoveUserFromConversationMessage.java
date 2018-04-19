package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionRemoveUserFromConversationMessage extends ActionServerMessage {
    private String username;
    private String conversationKey;

    public ActionRemoveUserFromConversationMessage(String username, String conversationKey) {
        this.status = ServerMessage.Status.ACTIONREMOVEDUSERFROMCONVERSATION.ordinal();
        this.username = username;
        this.conversationKey = conversationKey;
    }
}
