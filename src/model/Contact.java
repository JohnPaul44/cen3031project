package model;

import connection.serverMessages.notificationMessages.NotificationContactAddedMessage;
import connection.serverMessages.notificationMessages.NotificationUserOnlineStatusMessage;

import java.util.HashMap;

public class Contact {
    /*Username string `json:"username"`
    Online   bool   `json:"online"`*/

    private String username;
    private boolean online;
    private Profile profile;
    private FriendshipStats friendshipStats;

    // Test Constructor
    public Contact(String username, boolean online) {
        this.username = username;
        this.online = online;
    }

    public Contact(NotificationContactAddedMessage message) {
        username = message.getUsername();
        online = false;
        //profile = message.getProfile();
    }

    public String getUsername() {return username;}

    public boolean getOnline() {return online;}

    public Profile getProfile() {return profile;}

    public FriendshipStats getFriendshipStats() {return friendshipStats;}

    public void setFriendshipStats(FriendshipStats friendshipStats) {
        this.friendshipStats = friendshipStats;
    }

    public void updateOnline(NotificationUserOnlineStatusMessage message) {
        online = message.getOnline();
    }

}
