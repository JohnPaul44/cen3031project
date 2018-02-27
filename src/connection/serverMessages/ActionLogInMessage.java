package connection.serverMessages;

public class ActionLogInMessage extends ServerMessage {
    private String username;
    private String password;

    public ActionLogInMessage(String username, String password) {
        this.status = Status.ACTIONLOGIN;
        this.username = username;
        this.password = password;
    }
}
