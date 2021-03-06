package application;

import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import model.*;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

abstract public class ViewController {
    public void notification(ServerMessage message) {}

    //Utility Functions for using ArrayLists in Views
    public String ArrayListToString(ArrayList<String> list) {
        if(!(list==null)) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String s : list) {
                if(!first) {
                    sb.append(", ");
                }
                first = false;
                sb.append(s);
            }
            return sb.toString();
        }
        return "";
    }

    public ArrayList<String> StringToArrayList(String s) {
        return new ArrayList<>(Arrays.asList(s.split(", ")));
    }
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
                                            String time, String from, String text) {

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

    public void friendshipStatsNotification(ErrorInformation errorInformation, String username, FriendshipStats friendshipStats) {

    }
}