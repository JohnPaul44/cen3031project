package connection.serverMessages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MessageFactory {
    private Gson gson = new Gson();

    // TODO make proper exception
    public ServerMessage produce(JsonObject jsonObject) throws Exception {
        switch(jsonObject.get("status").getAsInt()) {
            case 1: // Error Message
                return gson.fromJson(jsonObject, NotificationErrorMessage.class);
            case 2: // Logged In Message
                return gson.fromJson(jsonObject, NotificationLoggedInMessage.class);
            case 3: // User Online Status
                return gson.fromJson(jsonObject, NotificationUserOnlineStatusMessage.class);
            case 4: // Logged Out Message
                return gson.fromJson(jsonObject, NotificationLoggedOutMessage.class);
            case 5: // Contact Added
                return gson.fromJson(jsonObject, NotificationContactAddedMessage.class);
            case 6: // Contact Removed
                return gson.fromJson(jsonObject, NotificationContactRemovedMessage.class);
            case 7: // Profile Updated
                return gson.fromJson(jsonObject, NotificationProfileUpdatedMessage.class);
            case 8: // Message Received
                return gson.fromJson(jsonObject, NotificationMessageReceivedMessage.class);
            case 9: // Message Updated
                return gson.fromJson(jsonObject, NotificationMessageUpdatedMessage.class);
            case 10: // User Added to Conversation
                return gson.fromJson(jsonObject, NotificationUserAddedToConversationMessage.class);
            case 11: // User Removed from Conversation
                return gson.fromJson(jsonObject, NotificationUserRemovedFromConversationMessage.class);
            case 12: // Message Read
                return gson.fromJson(jsonObject, NotificationMessageReadMessage.class);
            case 13: // Typing
                return gson.fromJson(jsonObject, NotificationTypingMessage.class);
            default:
                throw new Exception("Invalid message trying to be produced. Status of message: " + jsonObject.get("status").getAsInt());
        }
    }
}
