package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationUserOnlineStatusMessage;
import model.Contact;
import model.CurrentUser;


public class UserOnlineStatusMessageHandler implements NotificationMessageHandler {
    private NotificationUserOnlineStatusMessage message;
    private CurrentUser currentUser;

    public UserOnlineStatusMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.message = gson.fromJson(messageFromServer, NotificationUserOnlineStatusMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        Contact userToUpdateStatusOf = currentUser.getContactList().get(message.getUsername());
        userToUpdateStatusOf.setOnline(message.getOnline());
    }
}
