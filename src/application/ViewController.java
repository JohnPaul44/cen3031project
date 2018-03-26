package application;

import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import model.Contact;
import model.Conversation;
import model.Profile;
import model.Reactions;

import java.util.HashMap;
import java.util.Map;

abstract public class ViewController {
    public abstract void notification(ServerMessage message);


    /********************************************
     * All notifications will receive an integer error number and error string details. Not fully implemented yet
     */
    public void loggedInNotification(ErrorInformation errorInformation) {

    }

    public void userOnlineStatusNotification(ErrorInformation errorInformation, String username, boolean online) {

    }

    public void securityQuestionNotification(ErrorInformation errorInformation, String securityQuestion) {

    }

    public void passwordChangedNotification(ErrorInformation errorInformation) {

    }

    public void loggedOutNotification(ErrorInformation errorInformation) {

    }

    public void queryResultsNotification(ErrorInformation errorInformation, HashMap<String, Profile> results) {

    }

    public void contactAddedNotification(ErrorInformation errorInformation, String username) {

    }

    public void contactRemovedNotification(ErrorInformation errorInformation, String username) {

    }

    public void profileUpdatedNotification(ErrorInformation errorInformation, Profile profile) {

    }

    public void messageReceivedNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                            String time, String from, String text, Map<String, Reactions> reactions) {

    }

    public void messageUpdatedNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                           String text) {

    }

    public void messageReactionNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                            Map<String, Reactions> reactions) {

    }

    public void userAddedToConversationNotification(ErrorInformation errorInformation, String username, String conversationKey) {

    }

    public void userRemovedFromConversationNotification(ErrorInformation errorInformation, String username, String conversationKey,
                                                        Conversation conversation) {

    }

    public void messageReadNotification(ErrorInformation errorInformation, String conversationKey, String from) {

    }

    public void typingNotification(ErrorInformation errorInformation, String conversationKey, String from, boolean typing) {

    }

    public void contactUpdatedNotification(ErrorInformation errorInformation, HashMap<String, Contact> contacts) {

    }
}