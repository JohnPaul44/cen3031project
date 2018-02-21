package connection.serverMessages;

public class ActionRemoveUserFromConversationMessage extends ServerMessage {
    private String username;
    private String conversationKey;

    public ActionRemoveUserFromConversationMessage(String username, String conversationKey) {
        this.status = Status.ACTIONREMOVEDUSERFROMCONVERSATION.ordinal();
        this.username = username;
        this.conversationKey = conversationKey;
    }
}