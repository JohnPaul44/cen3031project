package connection.serverMessages;

public class ActionUpdateMessageMessage extends ActionServerMessage{
    private String conversationKey;
    private String messageKey;
    private String text;

    public ActionUpdateMessageMessage(String conversationKey, String messageKey, String text) {
        this.status = Status.ACTIONUPDATEMESSAGE.ordinal();
        this.conversationKey = conversationKey;
        this.messageKey = messageKey;
        this.text = text;
    }
}
