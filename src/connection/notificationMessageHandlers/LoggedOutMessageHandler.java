package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationLoggedOutMessage;
import model.CurrentUser;

public class LoggedOutMessageHandler implements MessageHandler {
    private NotificationLoggedOutMessage serverMessage;
    private CurrentUser currentUser;

    public LoggedOutMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationLoggedOutMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.clearUser();
    }


}
