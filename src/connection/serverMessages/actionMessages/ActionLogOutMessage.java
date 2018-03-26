package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionLogOutMessage extends ActionServerMessage{

    public ActionLogOutMessage() {
        this.status = ServerMessage.Status.ACTIONLOGOUT.ordinal();
    }
}
