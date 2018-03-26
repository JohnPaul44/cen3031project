package connection.serverMessages;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import connection.serverMessages.notificationMessages.*;

public class MessageFactory {
    private Gson gson = new Gson();

    // TODO make proper exception
    public ServerMessage produce(JsonObject jsonObject) throws Exception {
        int status = jsonObject.get("status").getAsInt();
        switch(ServerMessage.Status.values()[status]) {
            case NOTIFICATIONERROR: // Error Message
                return gson.fromJson(jsonObject, NotificationErrorMessage.class);
            case NOTIFICATIONLOGGEDIN: // Logged In Message
                return gson.fromJson(jsonObject, NotificationLoggedInMessage.class);
            case NOTIFICATIONSECURITYQUESTION: // Security Question
                return gson.fromJson(jsonObject, NotificationSecurityQuestion.class);
            case NOTIFICATIONPASSWORDCHANGED: // Password Changed
            case NOTIFICATIONUSERONLINESTATUS: // User Online Status
                return gson.fromJson(jsonObject, NotificationUserOnlineStatusMessage.class);
            case NOTIFICATIONLOGGEDOUT: // Logged Out Message
                return gson.fromJson(jsonObject, NotificationLoggedOutMessage.class);
            case NOTIFICATIONCONTACTADDED: // Contact Added
                return gson.fromJson(jsonObject, NotificationContactAddedMessage.class);
            case NOTIFICATIONCONTACTREMOVED: // Contact Removed
                return gson.fromJson(jsonObject, NotificationContactRemovedMessage.class);
            case NOTIFICATIONPROFILEUPDATED: // Profile Updated
                return gson.fromJson(jsonObject, NotificationProfileUpdatedMessage.class);
            case NOTIFICATIONMESSAGERECEIVED: // Message Received
                return gson.fromJson(jsonObject, NotificationMessageReceivedMessage.class);
            case NOTIFICATIONMESSAGEUPDATED: // Message Updated
                return gson.fromJson(jsonObject, NotificationMessageUpdatedMessage.class);
            case NOTIFICATIONMESSAGEREACTION: // Message Reaction
                return gson.fromJson(jsonObject, NotificationMessageReaction.class);
            case NOTIFICATIONUSERADDEDTOCONVERSATION: // User Added to Conversation
                return gson.fromJson(jsonObject, NotificationUserAddedToConversationMessage.class);
            case NOTIFICATIONUSERREMOVEDFROMCONVERSATION: // User Removed from Conversation
                return gson.fromJson(jsonObject, NotificationUserRemovedFromConversationMessage.class);
            case NOTIFICATIONMESSAGEREAD: // Message Read
                return gson.fromJson(jsonObject, NotificationMessageReadMessage.class);
            case NOTIFICATIONTYPING: // Typing
                return gson.fromJson(jsonObject, NotificationTypingMessage.class);
            default:
                throw new Exception("Invalid message trying to be produced. Status of message: " + jsonObject.get("status").getAsInt());
        }
    }
}
