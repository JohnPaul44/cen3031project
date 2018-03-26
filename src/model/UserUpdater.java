package model;

import connection.serverMessages.*;
import connection.serverMessages.notificationMessages.*;

import java.text.ParseException;

public class UserUpdater {
    private CurrentUser currentUser;

    public UserUpdater(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void updateUser(ServerMessage serverMessage) throws ParseException {
        switch (serverMessage.getStatus()) {
            case NOTIFICATIONLOGGEDIN: // Logged In Message
                currentUser.logIn((NotificationLoggedInMessage)serverMessage);
                break;
            case NOTIFICATIONUSERONLINESTATUS: // User Online Status
                currentUser.updateUserOnlineStatus((NotificationUserOnlineStatusMessage) serverMessage);
                break;
            case NOTIFICATIONLOGGEDOUT: // Logged Out Message
                currentUser.logOut();
                break;
            case NOTIFICATIONCONTACTADDED: // Contact Added
                currentUser.addContact((NotificationContactAddedMessage) serverMessage);
                break;
            case NOTIFICATIONCONTACTREMOVED: // Contact Removed
                currentUser.removeContact((NotificationContactRemovedMessage) serverMessage);
                break;
            case NOTIFICATIONPROFILEUPDATED: // Profile Updated
                currentUser.updateProfile((NotificationProfileUpdatedMessage) serverMessage);
                break;
            case NOTIFICATIONMESSAGERECEIVED: // Message Received
                currentUser.addMessage((NotificationMessageReceivedMessage) serverMessage);
                break;
            case NOTIFICATIONMESSAGEUPDATED: // Message Updated
                currentUser.updateMessage((NotificationMessageUpdatedMessage) serverMessage);
                break;
            case NOTIFICATIONUSERADDEDTOCONVERSATION: // User Added to Conversation
                currentUser.addUserToConversation((NotificationUserAddedToConversationMessage) serverMessage);
                break;
            case NOTIFICATIONUSERREMOVEDFROMCONVERSATION: // User Removed from Conversation
                currentUser.removeUserFromConversation((NotificationUserRemovedFromConversationMessage) serverMessage);
                break;
            case NOTIFICATIONMESSAGEREAD: // Message Read
                currentUser.updateMessageRead((NotificationMessageReadMessage) serverMessage);
                break;
            case NOTIFICATIONTYPING: // Typing
                currentUser.updateMessageTyping((NotificationTypingMessage) serverMessage);
        }
    }
}
