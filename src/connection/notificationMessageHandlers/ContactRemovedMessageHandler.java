package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationContactRemovedMessage;
import model.CurrentUser;

public class ContactRemovedMessageHandler implements MessageHandler {
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
