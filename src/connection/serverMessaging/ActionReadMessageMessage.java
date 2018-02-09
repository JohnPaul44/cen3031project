package connection.serverMessaging;

public class ActionReadMessageMessage extends ServerMessage {
    private String conversationKey;

    public ActionReadMessageMessage(String conversationKey) {
        this.status = Status.ACTIONREADMESSAGE.ordinal();
        this.conversationKey = conversationKey;
    }
}
