package connection.serverMessages;

public class ActionReadMessageMessage extends ServerMessage {
    private String conversationKey;

    public ActionReadMessageMessage(String conversationKey) {
        this.status = Status.ACTIONREADMESSAGE;
        this.conversationKey = conversationKey;
    }
}
