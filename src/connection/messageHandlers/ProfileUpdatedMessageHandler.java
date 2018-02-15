package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationProfileUpdatedMessage;
import model.CurrentUser;

public class ProfileUpdatedMessageHandler implements NotificationMessageHandler {
    private NotificationProfileUpdatedMessage serverMessage;
    private CurrentUser currentUser;

    public ProfileUpdatedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationProfileUpdatedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.setProfile(serverMessage.getProfile());
    }
}
