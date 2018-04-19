package connection;

import connection.serverMessages.ServerMessage;

public class ErrorInformation {
    private int errorNumber;
    private String errorString;

    public ErrorInformation() {
        errorNumber = 0;
        errorString = "";
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorInformation(ServerMessage serverMessage) {
        errorNumber = serverMessage.getErrorNumber();
        errorString = serverMessage.getErrorString();
    }
}
