package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionAddContactMessage extends ActionServerMessage{
    private String username;

    public ActionAddContactMessage(String username) {
        this.status = ServerMessage.Status.ACTIONADDCONTACT.ordinal();
        this.username = username;
    }
}
