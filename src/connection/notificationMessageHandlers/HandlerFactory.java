package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.CurrentUser;
import model.UserUpdater;

public class HandlerFactory {

    public MessageHandler produce(String serverMessage, UserUpdater userUpdater) throws Exception {
        JsonParser parser = new JsonParser();
        JsonObject messageFromServer = parser.parse(serverMessage).getAsJsonObject();
        int status = messageFromServer.get("status").getAsInt();

        switch (status) {
            case 0: // Uninitialized
                return new UninitializedMessageHandler();
            case 1: // Error Message
                return new ErrorMessageHandler(messageFromServer);
            case 2: // Logged In Message
            case 3: // User Online Status
            case 4: // Logged Out Message
            case 5: // Contact Added
            case 6: // Contact Removed
            case 7: // Profile Updated
            case 8: // Message Received
            case 9: // Message Updated
            case 10: // User Added to Conversation
            case 11: // User Removed from Conversation
            case 12: // Message Read
            case 13: // Typing
                return new ModelUpdateMessageHandler(messageFromServer, userUpdater);
            default:
                throw new Exception("Invalid message received. Status of message received: " + status);
        }
    }
}
