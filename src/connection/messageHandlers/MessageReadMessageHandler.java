package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationMessageReadMessage;
import model.CurrentUser;

public class MessageReadMessageHandler implements NotificationMessageHandler {
    private NotificationMessageReadMessage serverMessage;
    private CurrentUser currentUser;

    public MessageReadMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationMessageReadMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.getConversationList().get(serverMessage.getConversationKey()).getMemberStatus().get(serverMessage.getFrom()).setRead(true);
    }
}