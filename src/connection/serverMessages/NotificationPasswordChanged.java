package connection.serverMessages;

public class NotificationPasswordChanged extends ServerMessage {

    public NotificationPasswordChanged() {
        this.status = Status.NOTIFICATIONPASSWORDCHANGED.ordinal();
    }

}
