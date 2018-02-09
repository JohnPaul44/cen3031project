package connection.serverMessaging;

public class ActionRemoveContactMessage extends ServerMessage{
    private String username;

    public ActionRemoveContactMessage(String username) {
        this.status = Status.ACTIONREMOVECONTACT.ordinal();
        this.username = username;
    }
}
