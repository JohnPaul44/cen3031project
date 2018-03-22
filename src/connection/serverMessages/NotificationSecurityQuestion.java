package connection.serverMessages;

public class NotificationSecurityQuestion extends ServerMessage {
    private String securityQuestion;

    public NotificationSecurityQuestion(String securityQuestion) {
        this.status = Status.NOTIFICATIONSECURITYQUESTION.ordinal();
        this.securityQuestion = securityQuestion;
    }
}
