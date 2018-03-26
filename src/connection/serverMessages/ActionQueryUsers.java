package connection.serverMessages;

public class ActionQueryUsers extends ActionServerMessage {
    private String query;

    public ActionQueryUsers(String query) {
        this.status = Status.ACTIONQUERYUSERS.ordinal();
        this.query = query;
    }
}
