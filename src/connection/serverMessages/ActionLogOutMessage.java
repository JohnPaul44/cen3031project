package connection.serverMessages;

public class ActionLogOutMessage extends ActionServerMessage{

    public ActionLogOutMessage() {
        this.status = Status.ACTIONLOGOUT;
    }
}
