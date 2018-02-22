package connection.serverMessages;

public class ActionLogOutMessage extends ServerMessage{

    public ActionLogOutMessage() {
        this.status = Status.ACTIONLOGOUT.ordinal();
    }
}
