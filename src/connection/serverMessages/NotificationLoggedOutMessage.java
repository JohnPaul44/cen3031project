package connection.serverMessages;

public class NotificationLoggedOutMessage extends ServerMessage {

    public NotificationLoggedOutMessage() {
        this.status = Status.NOTIFICATIONLOGGEDOUT.ordinal();
    }
}
