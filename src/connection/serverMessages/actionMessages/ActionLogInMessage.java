package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionLogInMessage extends ActionServerMessage {
    private String username;
    private String password;

    public ActionLogInMessage(String username, String password) {
        this.status = ServerMessage.Status.ACTIONLOGIN.ordinal();
        this.username = username;
        this.password = password;
    }
}
