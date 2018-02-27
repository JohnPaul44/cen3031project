package connection.serverMessages;

public class ActionRemoveContactMessage extends ActionServerMessage{
    private String username;

    public ActionRemoveContactMessage(String username) {
        this.status = Status.ACTIONREMOVECONTACT;
        this.username = username;
    }
}
