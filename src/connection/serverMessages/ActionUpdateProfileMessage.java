package connection.serverMessages;

import model.Profile;

public class ActionUpdateProfileMessage extends ServerMessage{
    private Profile profile;

    public ActionUpdateProfileMessage(Profile profile) {
        this.status = Status.ACTIONUPDATEPROFILE.ordinal();
        this.profile = profile;
    }
}
