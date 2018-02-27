package connection.serverMessages;

import model.Profile;

public class ActionUpdateProfileMessage extends ActionServerMessage{
    private Profile profile;

    public ActionUpdateProfileMessage(Profile profile) {
        this.status = Status.ACTIONUPDATEPROFILE;
        this.profile = profile;
    }
}
