package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import connection.serverMessages.ServerMessage;
import model.CurrentUser;
import model.UserUpdater;

public class HandlerFactory {

    public MessageHandler produce(String serverMessage, UserUpdater userUpdater) throws Exception {
        JsonParser parser = new JsonParser();
        JsonObject messageFromServer = parser.parse(serverMessage).getAsJsonObject();
        int status = messageFromServer.get("status").getAsInt();

        switch (ServerMessage.Status.values()[status]) {
            case UNINITILIALIZED: // Uninitialized
                return new UninitializedMessageHandler();
            case NOTIFICATIONERROR: // Error Message
                return new ErrorMessageHandler(messageFromServer);
            case NOTIFICATIONLOGGEDIN: // Logged In Notification
            case NOTIFICATIONSECURITYQUESTION: // Security Question Notification
            case NOTIFICATIONPASSWORDCHANGED: // Password Changed Notification
            case NOTIFICATIONUSERONLINESTATUS: // User Online Status Notification
            case NOTIFICATIONLOGGEDOUT: // Logged Out Message Notification
            case NOTIFICATIONCONTACTADDED: // Contact Added Notification
            case NOTIFICATIONCONTACTREMOVED: // Contact Removed Notification
            case NOTIFICATIONPROFILEUPDATED: // Profile Updated Notification
            case NOTIFICATIONMESSAGERECEIVED: // Message Received Notification
            case NOTIFICATIONMESSAGEUPDATED: // Message Updated Notification
            case NOTIFICATIONMESSAGEREACTION: // Message Reaction Notification
            case NOTIFICATIONUSERADDEDTOCONVERSATION: // User Added to Conversation Notification
            case NOTIFICATIONUSERREMOVEDFROMCONVERSATION: // User Removed from Conversation Notification
            case NOTIFICATIONMESSAGEREAD: // Message Read Notification
            case NOTIFICATIONQUERYRESULTS: //Query Results Notification
            case NOTIFICATIONTYPING: // Typing Notification
                System.out.println("Message received from server with status: " + status);
                return new ModelUpdateMessageHandler(messageFromServer, userUpdater);
            case ACTIONREGISTER: // Register Action
            case ACTIONLOGIN: // Log In Action
            case ACTIONREQUESTSECURITYQUESTION: // Request Security Question Action
            case ACTIONCHANGEPASSWORD: // Change Password Action
            case ACTIONLOGOUT: // Log Out Action
            case ACTIONADDCONTACT: // Add Contact Action
            case ACTIONREMOVECONTACT: // Remove Contact Action
            case ACTIONUPDATEPROFILE: // Update Profile Action
            case ACTIONSENDMESSAGE: // Send Message Action
            case ACTIONUPDATEMESSAGE: // Update Message Action
            case ACTIONREACTTOMESSAGE: // React To Message
            case ACTIONADDUSERTOCONVERSATION: // Add User To Conversation Action
            case ACTIONREMOVEDUSERFROMCONVERSATION: // Remove User From Conversation Action
            case ACTIONREADMESSAGE: // Read Message Action
            case ACTIONSETTYPING: // Set Typing Action
            case ACTIONQUERYUSERS: //Query Users Action
                throw new Exception("Action message received. Status of message received: " + status);
            default:
                throw new Exception("Invalid message received. Status of message received: " + status);
        }
    }
}
