package connection.serverMessaging;

public class NotificationUserOnlineStatusMessage extends ServerMessage {
    private boolean online;
    private String username;

    public NotificationUserOnlineStatusMessage(boolean online, String username) {
        this.status = Status.NOTIFICATIONUSERONLINESTATUS.ordinal();
        this.online = online;
        this.username = username;
    }

    public boolean getOnline() {
        return online;
    }
    public String getUsername() {
        return username;
    }
}
