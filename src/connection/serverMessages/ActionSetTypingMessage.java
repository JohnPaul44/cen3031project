package connection.serverMessages;

public class ActionSetTypingMessage extends ServerMessage{
    private String conversationKey;
    private boolean typing;

    public ActionSetTypingMessage(String conversationKey, boolean typing) {
        this.status = Status.ACTIONSETTYPING;
        this.conversationKey = conversationKey;
        this.typing = typing;
    }
}
