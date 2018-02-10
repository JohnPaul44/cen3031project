package connection.serverMessaging;

public class NotificationContactAddedMessage extends ServerMessage {
    private String username;

    public NotificationContactAddedMessage(String username) {
        this.status = Status.NOTIFICATIONCONTACTADDED.ordinal();
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
