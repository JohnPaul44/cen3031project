package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationLoggedInMessage;
import model.CurrentUser;

public class LoggedInMessageHandler implements MessageHandler {
    private NotificationLoggedInMessage serverMessage;
    private CurrentUser currentUser;

    public LoggedInMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationLoggedInMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.setUserName(serverMessage.getUsername());
        currentUser.setContactList(serverMessage.getContacts());
        currentUser.setConversationList(serverMessage.getConversations());
        currentUser.setProfile(serverMessage.getProfile());
    }
}
