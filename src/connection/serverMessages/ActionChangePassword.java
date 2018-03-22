package connection.serverMessages;

public class ActionChangePassword extends ActionServerMessage {
    private String username;
    private String securityAnswer;
    private String phone;
    private String password;

    public ActionChangePassword(String username, String securityAnswer, String phone, String password) {
        this.status = Status.ACTIONCHANGEPASSWORD.ordinal();
        this.username = username;
        this.securityAnswer = securityAnswer;
        this.phone = phone;
        this.password = password;
    }

}
