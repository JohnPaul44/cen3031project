package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationTypingMessage;
import model.CurrentUser;

public class MessageTypingMessageHandler implements MessageHandler {
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
