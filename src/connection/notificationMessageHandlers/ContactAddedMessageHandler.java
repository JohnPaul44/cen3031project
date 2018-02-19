package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationContactAddedMessage;
import model.Contact;
import model.CurrentUser;

public class ContactAddedMessageHandler implements NotificationMessageHandler {
    private NotificationContactAddedMessage serverMessage;
    private CurrentUser currentUser;

    public ContactAddedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationContactAddedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle(){
        Contact contact = new Contact(serverMessage.getUsername(), false);
        currentUser.getContactList().put(serverMessage.getUsername(), contact);
    }
}
