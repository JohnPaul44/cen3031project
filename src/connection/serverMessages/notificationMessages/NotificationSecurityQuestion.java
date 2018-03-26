package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;

public class NotificationSecurityQuestion extends ServerMessage {
    private String securityQuestion;

    public NotificationSecurityQuestion(String securityQuestion) {
        this.status = Status.NOTIFICATIONSECURITYQUESTION.ordinal();
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }
}
