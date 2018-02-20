package model;

import connection.serverMessages.NotificationContactAddedMessage;
import connection.serverMessages.NotificationUserOnlineStatusMessage;

public class Contact {
    /*Username string `json:"username"`
    Online   bool   `json:"online"`*/

    private String username;
    private boolean online;

    // Test Constructor
    public Contact(String username, boolean online) {
        this.username = username;
        this.online = online;
    }

    public Contact(NotificationContactAddedMessage message) {
        username = message.getUsername();
        online = false;
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
