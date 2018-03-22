package connection.serverMessages;

public class ActionRequestSecurityQuestion extends ActionServerMessage{
    private String username;

    public ActionRequestSecurityQuestion(String username) {
        this.status = Status.ACTIONREQUESTSECURITYQUESTION.ordinal();
        this.username = username;
    }
}
