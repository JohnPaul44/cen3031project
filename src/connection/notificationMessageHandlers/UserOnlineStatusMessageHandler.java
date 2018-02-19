package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationUserOnlineStatusMessage;
import model.Contact;
import model.CurrentUser;


public class UserOnlineStatusMessageHandler implements NotificationMessageHandler {
    private NotificationUserOnlineStatusMessage serverMessage;
    private CurrentUser currentUser;

    public UserOnlineStatusMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationUserOnlineStatusMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        Contact userToUpdateStatusOf = currentUser.getContactList().get(serverMessage.getUsername());
        userToUpdateStatusOf.setOnline(serverMessage.getOnline());
    }
}
