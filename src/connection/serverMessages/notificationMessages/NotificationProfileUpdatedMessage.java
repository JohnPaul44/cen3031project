package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Profile;

public class NotificationProfileUpdatedMessage extends ServerMessage {
    private Profile profile;

    public NotificationProfileUpdatedMessage(Profile profile) {
        this.status = Status.NOTIFICATIONPROFILEUPDATED.ordinal();
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
    }
}
