package connection.MessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationLoggedOutMessage;
import model.CurrentUser;

public class LoggedOutMessageHandler implements NotificationMessageHandler {
    private NotificationLoggedOutMessage message;
    private CurrentUser currentUser;

    public LoggedOutMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.message = gson.fromJson(messageFromServer, NotificationLoggedOutMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        System.out.println("Logged out message received from Server");
        currentUser.clearUser();
    }


}
