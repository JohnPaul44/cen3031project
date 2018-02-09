package connection.serverMessaging;

public class ActionRegisterMessage extends ServerMessage{
    private String username;
    private String password;
    private String name;
    private String email;

    public ActionRegisterMessage(String username, String password, String name, String email) {
        this.status = Status.ACTIONREGISTER.ordinal();
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
    }
}
