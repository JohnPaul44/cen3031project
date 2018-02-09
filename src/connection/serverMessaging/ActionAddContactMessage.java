package connection.serverMessaging;

public class ActionAddContactMessage extends ServerMessage{
    private String username;

    public ActionAddContactMessage(String username) {
        this.status = Status.ACTIONADDCONTACT.ordinal();
        this.username = username;
    }
}
