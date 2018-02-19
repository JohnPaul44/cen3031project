package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationProfileUpdatedMessage;
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
