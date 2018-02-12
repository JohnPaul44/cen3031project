package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationContactRemovedMessage;
import model.CurrentUser;

public class ContactRemovedMessageHandler implements NotificationMessageHandler {
    private NotificationContactRemovedMessage message;
    private CurrentUser currentUser;

    public ContactRemovedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.message = gson.fromJson(messageFromServer, NotificationContactRemovedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.getContactList().remove(message.getUsername());
    }
}
