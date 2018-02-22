package connection.serverMessages;

public class ActionAddUserToConversationMessage extends ServerMessage {
    private String username;
    private String conversationKey;

    public ActionAddUserToConversationMessage(String username, String conversationKey) {
        this.status = Status.ACTIONADDUSERTOCONVERSATION.ordinal();
        this.username = username;
        this.conversationKey = conversationKey;
    }
}
