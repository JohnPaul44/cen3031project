package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;

public class NotificationContactRemovedMessage extends ServerMessage {
    private String username;

    public NotificationContactRemovedMessage(String username) {
        this.status = Status.NOTIFICATIONCONTACTREMOVED.ordinal();
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
