package connection.serverMessages;

public class ActionReadMessageMessage extends ActionServerMessage {
    private String conversationKey;

    public ActionReadMessageMessage(String conversationKey) {
        this.status = Status.ACTIONREADMESSAGE;
        this.conversationKey = conversationKey;
    }
}
