package connection.serverMessages.actionMessages;

import connection.serverMessages.actionMessages.ActionServerMessage;

public class ActionQueryUsersMessage extends ActionServerMessage {
    private String query;

    public ActionQueryUsersMessage(String query) {
        this.status = Status.ACTIONQUERYUSERS.ordinal();
        this.query = query;
    }
}
