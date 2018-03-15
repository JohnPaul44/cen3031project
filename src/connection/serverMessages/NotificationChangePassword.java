package connection.serverMessages;

public class NotificationChangePassword extends ServerMessage {
    private String securityQuestion;

    public NotificationChangePassword(String securityQuestion) {
        this.status = Status.NOTIFICATIONCHANGEPASSWORD.ordinal();
        this.securityQuestion = securityQuestion;
    }
}
