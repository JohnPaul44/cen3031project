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
            case 2: // Logged In Notification
            case 3: // User Online Status Notification
            case 4: // Logged Out Message Notification
            case 5: // Contact Added Notification
            case 6: // Contact Removed Notification
            case 7: // Profile Updated Notification
            case 8: // Message Received Notification
            case 9: // Message Updated Notification
            case 10: // Message Reaction Notification
            case 11: // User Added to Conversation Notification
            case 12: // User Removed from Conversation Notification
            case 13: // Message Read Notification
            case 14: // Typing Notification
                System.out.println("Message received from server with status: " + status);
                return new ModelUpdateMessageHandler(messageFromServer, userUpdater);
            case 15: // Register Action
            case 16: // Log In Action
            case 17: // Log Out Action
            case 18: // Add Contact Action
            case 19: // Remove Contact Action
            case 20: // Update Profile Action
            case 21: // Send Message Action
            case 22: // Update Message Action
            case 23: // React To Message
            case 24: // Add User To Conversation Action
            case 25: // Remove User From Conversation Action
            case 26: // Read Message Action
            case 27: // Set Typing Action
                throw new Exception("Action message received. Status of message received: " + status);
            default:
                throw new Exception("Invalid message received. Status of message received: " + status);
        }
    }
}
