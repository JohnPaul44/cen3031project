package connection.serverMessages;

public class NotificationContactRemovedMessage extends ServerMessage {
    private String username;

    public NotificationContactRemovedMessage(String username) {
        this.status = Status.NOTIFICATIONCONTACTREMOVED;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
