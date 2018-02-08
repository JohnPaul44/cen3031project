package sample;

public class LoginServerMessage extends ServerMessage {
    private String username;
    private String password;

    public LoginServerMessage(String username, String password) {
        this.status = Status.LOGIN.ordinal();
        this.username = username;
        this.password = password;
    }

    public void printUsername() {
        System.out.println(username);
    }
}
