package connection.serverMessages;

public class ActionLogInMessage extends ActionServerMessage {
    private String username;
    private String password;

    public ActionLogInMessage(String username, String password) {
        this.status = Status.ACTIONLOGIN.ordinal();
        this.username = username;
        this.password = password;
    }
}
