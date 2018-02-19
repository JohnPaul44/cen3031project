package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationMessageUpdatedMessage;
import model.CurrentUser;
import model.Message;

public class MessageUpdatedMessageHandler implements MessageHandler {
    private NotificationMessageUpdatedMessage serverMessage;
    private CurrentUser currentUser;

    public MessageUpdatedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationMessageUpdatedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() {
        Message m = currentUser.getConversationList().get(serverMessage.getConversationKey()).getMessages().get(serverMessage.getMessageKey());
        m.setText(serverMessage.getText());
    }
}
