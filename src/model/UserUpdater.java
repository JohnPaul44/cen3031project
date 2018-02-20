package model;

import connection.serverMessages.*;

import java.text.ParseException;

public class UserUpdater {
    private CurrentUser currentUser;

    public UserUpdater(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void updateUser(ServerMessage serverMessage) throws ParseException {
        System.out.println(serverMessage.getStatus());
        switch (serverMessage.getStatus()) {
            case 2: // Logged In Message
                System.out.println("hello");
                currentUser.logIn((NotificationLoggedInMessage)serverMessage);
                break;
            /*case 3: // User Online Status
                currentUser.updateUserOnlineStatus((NotificationUserOnlineStatusMessage) serverMessage);
                break;
            case 4: // Logged Out Message
                currentUser.logOut();
                break;
            case 5: // Contact Added
                currentUser.addContact((NotificationContactAddedMessage) serverMessage);
                break;
            case 6: // Contact Removed
                currentUser.removeContact((NotificationContactRemovedMessage) serverMessage);
                break;
            case 7: // Profile Updated
                currentUser.updateProfile((NotificationProfileUpdatedMessage) serverMessage);
                break;
            case 8: // Message Received
                currentUser.addMessage((NotificationMessageReceivedMessage) serverMessage);
                break;
            case 9: // Message Updated
                currentUser.updateMessage((NotificationMessageUpdatedMessage) serverMessage);
                break;
            case 10: // User Added to Conversation
                currentUser.addUserToConversation((NotificationUserAddedToConversationMessage) serverMessage);
                break;
            case 11: // User Removed from Conversation
                currentUser.removeUserFromConversation((NotificationUserRemovedFromConversationMessage) serverMessage);
                break;
            case 12: // Message Read
                currentUser.updateMessageRead((NotificationMessageReadMessage) serverMessage);
                break;
            case 13: // Typing
                currentUser.updateMessageTyping((NotificationTypingMessage) serverMessage);*/
        }
    }
}
