package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationMessageReceivedMessage;
import model.Conversation;
import model.CurrentUser;
import model.Message;

import java.text.ParseException;

public class MessageReceivedMessageHandler implements NotificationMessageHandler {
    private NotificationMessageReceivedMessage message;
    private CurrentUser currentUser;

    public MessageReceivedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.message = gson.fromJson(messageFromServer, NotificationMessageReceivedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() { // TODO what if message if first message in group conversation
        Message receivedMessage = new Message(message);
        if (currentUser.getConversationList().get(message.getConversationKey()) != null) { // existing conversation
            try {
                currentUser.getConversationList().get(message.getConversationKey()).addMessageToConversation(receivedMessage);
            } catch (ParseException e) {
                System.out.println("Error parsing date while adding message to conversation" + e);
            }
        } else { // new conversation
            Conversation newConversation = new Conversation(message);
            currentUser.getConversationList().put(receivedMessage.getConversationKey(), newConversation);
        }
    }
}
