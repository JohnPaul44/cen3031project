package model;

import connection.serverMessages.notificationMessages.NotificationContactAddedMessage;
import connection.serverMessages.notificationMessages.NotificationUserOnlineStatusMessage;

import java.util.HashMap;

public class Contact {
    /*Username string `json:"username"`
    Online   bool   `json:"online"`*/

    private String username;
    private boolean online;
    private int sentMessages;
    private int receivedMessages;
    private HashMap<String, ContactGame> games;
    private int friendshipLevel;

    // Test Constructor
    public Contact(String username, boolean online) {
        this.username = username;
        this.online = online;
    }

    public Contact(NotificationContactAddedMessage message) {
        username = message.getUsername();
        online = false;
        sentMessages = 0;
        receivedMessages = 0;
        games = new HashMap<>();
        friendshipLevel = 0;
    }

    public String getUsername() {
        return username;
    }
    public boolean getOnline() {
        return online;
    }

    public void updateOnline(NotificationUserOnlineStatusMessage message) {
        online = message.getOnline();
    }
}
