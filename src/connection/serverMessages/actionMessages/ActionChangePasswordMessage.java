package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionChangePasswordMessage extends ActionServerMessage {
    private String username;
    private String securityAnswer;
    private String phone;
    private String password;

    public ActionChangePasswordMessage(String username, String securityAnswer, String phone, String password) {
        this.status = ServerMessage.Status.ACTIONCHANGEPASSWORD.ordinal();
        this.username = username;
        this.securityAnswer = securityAnswer;
        this.phone = phone;
        this.password = password;
    }

}
