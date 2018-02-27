package connection.serverMessages;

public class ActionAddContactMessage extends ActionServerMessage{
    private String username;

    public ActionAddContactMessage(String username) {
        this.status = Status.ACTIONADDCONTACT;
        this.username = username;
    }
}
