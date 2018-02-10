package connection.serverMessaging;

public class NotificationLoggedOutMessage extends ServerMessage {

    public NotificationLoggedOutMessage() {
        this.status = Status.NOTIFICATIONLOGGEDOUT.ordinal();
    }
}
