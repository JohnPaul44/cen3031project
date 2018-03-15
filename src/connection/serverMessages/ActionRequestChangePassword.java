package connection.serverMessages;

public class ActionRequestChangePassword extends ActionServerMessage{
    private String username;

    public ActionRequestChangePassword(String username) {
        this.status = Status.ACTIONREQUESTCHANGEPASSWORD.ordinal();
        this.username = username;
    }
}
