package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationContactRemovedMessage;
import model.CurrentUser;

public class ContactRemovedMessageHandler implements NotificationMessageHandler {
    private NotificationContactRemovedMessage serverMessage;
    private CurrentUser currentUser;

    public ContactRemovedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationContactRemovedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.getContactList().remove(serverMessage.getUsername());
    }
}
