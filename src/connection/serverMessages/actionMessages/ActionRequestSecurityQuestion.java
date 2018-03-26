package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;

public class ActionRequestSecurityQuestion extends ActionServerMessage{
    private String username;

    public ActionRequestSecurityQuestion(String username) {
        this.status = ServerMessage.Status.ACTIONREQUESTSECURITYQUESTION.ordinal();
        this.username = username;
    }
}
