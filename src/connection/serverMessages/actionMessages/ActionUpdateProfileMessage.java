package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;
import model.Profile;

public class ActionUpdateProfileMessage extends ActionServerMessage{
    private Profile profile;

    public ActionUpdateProfileMessage(Profile profile) {
        this.status = ServerMessage.Status.ACTIONUPDATEPROFILE.ordinal();
        this.profile = profile;
    }
}
