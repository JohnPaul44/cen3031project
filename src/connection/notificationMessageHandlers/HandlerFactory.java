package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import connection.serverMessages.ServerMessage;
import model.CurrentUser;
import model.UserUpdater;

public class HandlerFactory {

    public MessageHandler produce(ServerMessage serverMessage, UserUpdater userUpdater) throws Exception {
        System.out.println("Message received from server with status: " + serverMessage.getStatus());
        if (serverMessage.error()) {
            System.out.println("Message received from server has error: " + serverMessage.errorString);
        }
        switch (serverMessage.getStatus()) {
            case UNINITILIALIZED: // Uninitialized
                return new UninitializedMessageHandler();
            case NOTIFICATIONERROR: // Error Message
                return new ErrorMessageHandler(serverMessage);
            case NOTIFICATIONLOGGEDIN: // Logged In Notification
                return new LoggedInMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONSECURITYQUESTION: // Security Question Notification
                return new SecurityQuestionMessageHandler(serverMessage);
            case NOTIFICATIONPASSWORDCHANGED: // Password Changed Notification
                return new PasswordChangedMessageHandler(serverMessage);
            case NOTIFICATIONUSERONLINESTATUS: // User Online Status Notification
                return new UserOnlineStatusMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONLOGGEDOUT: // Logged Out Message Notification
                return new LoggedOutMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONCONTACTADDED: // Contact Added Notification
                return new ContactAddedMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONCONTACTREMOVED: // Contact Removed Notification
                return new ContactRemovedMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONPROFILEUPDATED: // Profile Updated Notification
                return new ProfileUpdatedMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONMESSAGERECEIVED: // Message Received Notification
                return new MessageReceivedMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONMESSAGEUPDATED: // Message Updated Notification
                return new MessageUpdatedMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONMESSAGEREACTION: // Message Reaction Notification
                return new MessageReactionMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONUSERADDEDTOCONVERSATION: // User Added to Conversation Notification
                return new UserAddedToConversationMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONUSERREMOVEDFROMCONVERSATION: // User Removed from Conversation Notification
                return new UserRemovedFromConversationMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONMESSAGEREAD: // Message Read Notification
                return new MessageReadMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONQUERYRESULTS: //Query Results Notification
                return new QueryResultsMessageHandler(serverMessage);
            case NOTIFICATIONTYPING: // Typing Notification
                return new TypingMessageHandler(serverMessage, userUpdater);
            case NOTIFICATIONFRIENDSHIPSTATS: // Contact Updated
                return new FriendshipStatsMessageHandler(serverMessage, userUpdater);
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
                throw new Exception("Action message received. Status of message received: " + serverMessage.getStatus().ordinal());
            default:
                throw new Exception("Invalid message received. Status of message received: " + serverMessage.getStatus().ordinal());
        }
    }
}
