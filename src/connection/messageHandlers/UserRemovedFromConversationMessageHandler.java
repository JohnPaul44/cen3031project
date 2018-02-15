package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationUserRemovedFromConversationMessage;
import model.CurrentUser;

public class UserRemovedFromConversationMessageHandler implements NotificationMessageHandler {
    private NotificationUserRemovedFromConversationMessage serverMessage;
    private CurrentUser currentUser;

    public UserRemovedFromConversationMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationUserRemovedFromConversationMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        if (currentUser.getUserName().equals(serverMessage.getUsername())) { // user to be removed is current user
            currentUser.getConversationList().remove(serverMessage.getConversationKey());
        } else { // user to be removed is not current user
            currentUser.getConversationList().get(serverMessage.getConversationKey()).getMemberStatus().remove(serverMessage.getUsername());
        }
    }
}
