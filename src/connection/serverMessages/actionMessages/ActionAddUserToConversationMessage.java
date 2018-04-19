package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionAddUserToConversationMessage extends ActionServerMessage {
    private String username;
    private String conversationKey;

    public ActionAddUserToConversationMessage(String username, String conversationKey) {
        this.status = ServerMessage.Status.ACTIONADDUSERTOCONVERSATION.ordinal();
        this.username = username;
        this.conversationKey = conversationKey;
    }
}
