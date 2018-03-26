package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;

public class NotificationErrorMessage extends ServerMessage {
    private int errorNumber;
    private String errorString;

    public NotificationErrorMessage(int errorNumber, String errorString) {
        this.status = Status.NOTIFICATIONERROR.ordinal();
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
