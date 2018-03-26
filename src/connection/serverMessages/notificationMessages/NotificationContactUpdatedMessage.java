package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Contact;

import java.util.HashMap;

public class NotificationContactUpdatedMessage extends ServerMessage {
    private HashMap<String, Contact> contacts;

    public NotificationContactUpdatedMessage(HashMap<String, Contact> contacts) {
        this.status = Status.NOTIFICATIONCONTACTUPDATED.ordinal();
        this.contacts = contacts;
    }

    public HashMap<String, Contact> getContacts() {
        return contacts;
    }
}
