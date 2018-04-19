package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionRemoveContactMessage extends ActionServerMessage{
    private String username;

    public ActionRemoveContactMessage(String username) {
        this.status = ServerMessage.Status.ACTIONREMOVECONTACT.ordinal();
        this.username = username;
    }
}
