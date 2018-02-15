package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationTypingMessage;
import model.CurrentUser;

public class MessageTypingMessageHandler implements NotificationMessageHandler {
    private NotificationTypingMessage serverMessage;
    private CurrentUser currentUser;

    public MessageTypingMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationTypingMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        currentUser.getConversationList().get(serverMessage.getConversationKey()).getMemberStatus().get(serverMessage.getFrom()).setTyping(serverMessage.getTyping());
    }
}
