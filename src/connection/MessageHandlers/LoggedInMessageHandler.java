package connection.MessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationLoggedInMessage;
import model.CurrentUser;

public class LoggedInMessageHandler implements NotificationMessageHandler {
    private NotificationLoggedInMessage message;
    private CurrentUser currentUser;

    public LoggedInMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.message = gson.fromJson(messageFromServer, NotificationLoggedInMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.setUserName(message.getUsername());
        currentUser.setContactList(message.getContacts());
        currentUser.setConversationList(message.getConversations());
        currentUser.setProfile(message.getProfile());
    }
}
