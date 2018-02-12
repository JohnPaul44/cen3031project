package connection.serverMessaging;

import model.Contact;
import model.Conversation;
import model.Profile;

import java.util.HashMap;

public class NotificationLoggedInMessage extends ServerMessage {
    private String username;
    private Profile profile;
    private HashMap<String, Contact> contacts;
    private HashMap<String, Conversation> conversations;

    public NotificationLoggedInMessage(String username, Profile profile, HashMap<String, Contact> contacts, HashMap<String, Conversation> conversations) {
        this.status = Status.NOTIFICATIONLOGGEDIN.ordinal();
        this.username = username;
        this.profile = profile;
        this.contacts = contacts;
        this.conversations = conversations;
    }

    public String getUsername() {
        return username;
    }
    public Profile getProfile() {
        return profile;
    }
    public HashMap<String, Contact> getContacts() {
        return contacts;
    }
    public HashMap<String, Conversation> getConversations() {
        return conversations;
    }
}
