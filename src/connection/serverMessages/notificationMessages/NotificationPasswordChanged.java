package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;

public class NotificationPasswordChanged extends ServerMessage {

    public NotificationPasswordChanged() {
        this.status = Status.NOTIFICATIONPASSWORDCHANGED.ordinal();
    }

}
