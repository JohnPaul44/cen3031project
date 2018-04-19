package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;

public class NotificationLoggedOutMessage extends ServerMessage {

    public NotificationLoggedOutMessage() {
        this.status = Status.NOTIFICATIONLOGGEDOUT.ordinal();
    }
}
