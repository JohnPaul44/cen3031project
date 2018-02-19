package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationUserAddedToConversationMessage;
import model.Conversation;
import model.CurrentUser;
import model.Status;

public class UserAddedToConversationMessageHandler implements NotificationMessageHandler {
    private NotificationUserAddedToConversationMessage serverMessage;
    private CurrentUser currentUser;

    public UserAddedToConversationMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationUserAddedToConversationMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        if (serverMessage.getConversation() == null) { // conversation already exists
            Conversation c = currentUser.getConversationList().get(serverMessage.getConversationKey());
            c.getMemberStatus().put(serverMessage.getUsername(), new Status(false, false));
        } else { // new conversation
            currentUser.getConversationList().put(serverMessage.getConversation().getConversationKey(), serverMessage.getConversation());
        }
    }
}
