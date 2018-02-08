package sample;

public class LoginMessage extends Message {
    private String username;
    private String password;

    public LoginMessage(String username, String password) {
        this.status = Status.LOGIN.ordinal();
        this.username = username;
        this.password = password;
    }

    public void printUsername() {
        System.out.println(username);
    }
}
