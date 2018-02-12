package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationContactAddedMessage;
import model.Contact;
import model.CurrentUser;

public class ContactAddedMessageHandler implements NotificationMessageHandler {
    private NotificationContactAddedMessage message;
    private CurrentUser currentUser;

    public ContactAddedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.message = gson.fromJson(messageFromServer, NotificationContactAddedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle(){
        Contact contact = new Contact(message.getUsername(), false);
        currentUser.getContactList().put(message.getUsername(), contact);
    }
}
