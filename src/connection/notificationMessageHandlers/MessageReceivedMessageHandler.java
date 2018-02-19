package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationMessageReceivedMessage;
import model.Conversation;
import model.CurrentUser;
import model.Message;

import java.text.ParseException;

public class MessageReceivedMessageHandler implements MessageHandler {
    private NotificationMessageReceivedMessage serverMessage;
    private CurrentUser currentUser;

    public MessageReceivedMessageHandler(JsonObject messageFromServer, CurrentUser currentUser) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationMessageReceivedMessage.class);
        this.currentUser = currentUser;
    }

    @Override
    public void handle() { // TODO what if serverMessage is first serverMessage in group conversation
        Message receivedMessage = new Message(serverMessage);
        if (serverMessage.getConversation() == null) { // existing conversation
            try {
                currentUser.getConversationList().get(serverMessage.getConversationKey()).addMessageToConversation(receivedMessage);
            } catch (ParseException e) {
                System.out.println("Error parsing date while adding serverMessage to conversation" + e);
            }
        } else { // new conversation
            Conversation newConversation = serverMessage.getConversation();
            System.out.println("setting conversation worked");
            currentUser.getConversationList().put(newConversation.getConversationKey(), newConversation);
        }
    }
}
