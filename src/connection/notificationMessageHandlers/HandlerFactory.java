package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.CurrentUser;

public class HandlerFactory {

    public MessageHandler produce(String serverMessage, CurrentUser currentUser) throws Exception {
        JsonParser parser = new JsonParser();
        JsonObject messageFromServer = parser.parse(serverMessage).getAsJsonObject();
        int status = messageFromServer.get("status").getAsInt();

        switch (status) {
            case 0: // Uninitialized
                return new UninitializedMessageHandler();
            case 1: // Error Message
                return new ErrorMessageHandler(messageFromServer);
            case 2: // Logged In Message
                return new LoggedInMessageHandler(messageFromServer, currentUser);
            case 3: // User Online Status
                return new UserOnlineStatusMessageHandler(messageFromServer, currentUser);
            case 4: // Logged Out Message
                return new LoggedOutMessageHandler(messageFromServer, currentUser);
            case 5: // Contact Added
                return new ContactAddedMessageHandler(messageFromServer, currentUser);
            case 6: // Contact Removed
                return new ContactRemovedMessageHandler(messageFromServer, currentUser);
            case 7: // Profile Updated
                return new ProfileUpdatedMessageHandler(messageFromServer, currentUser);
            case 8: // Message Received
                return new MessageReceivedMessageHandler(messageFromServer, currentUser);
            case 9: // Message Updated
                return new MessageUpdatedMessageHandler(messageFromServer, currentUser);
            case 10: // User Added to Conversation
                return new UserAddedToConversationMessageHandler(messageFromServer, currentUser);
            case 11: // User Removed from Conversation
                return new UserRemovedFromConversationMessageHandler(messageFromServer, currentUser);
            case 12: // Message Read
                return new MessageReadMessageHandler(messageFromServer, currentUser);
            case 13: // Typing
                return new MessageTypingMessageHandler(messageFromServer, currentUser);
            default:
                throw new Exception("Invalid message received. Status of message received: \" + status");
        }
    }
}
