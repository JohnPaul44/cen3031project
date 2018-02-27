package connection.serverMessages;

public class NotificationErrorMessage extends ServerMessage {
    private int errorNumber;
    private String errorString;

    public NotificationErrorMessage(int errorNumber, String errorString) {
        this.status = Status.NOTIFICATIONERROR;
        this.errorNumber = errorNumber;
        this.errorString = errorString;
    }

    public int getErrorNumber() {
        return errorNumber;
    }
    public String getErrorString() {
        return errorString;
    }
}
