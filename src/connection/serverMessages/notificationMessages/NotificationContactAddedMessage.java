package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Profile;

public class NotificationContactAddedMessage extends ServerMessage {
    private String username;
    private Profile profile;

    public NotificationContactAddedMessage(String username, Profile profile) {
        this.status = Status.NOTIFICATIONCONTACTADDED.ordinal();
        this.username = username;
        this.profile = profile;
    }

    public String getUsername() {
        return username;
    }
    public Profile getProfile() {
        return profile;
    }
}
