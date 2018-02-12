package connection.serverMessaging;

import model.Contact;
import model.Conversation;
import model.Profile;

import java.util.ArrayList;
import java.util.HashMap;

public class NotificationLoggedInMessage extends ServerMessage {
    private String username;
    private Profile profile;
    private HashMap<String, Contact> contacts;
    private ArrayList<Conversation> conversations;

    public NotificationLoggedInMessage(String username, Profile profile, HashMap<String, Contact> contacts, ArrayList<Conversation> conversations) {
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
    public ArrayList<Conversation> getConversations() {
        return conversations;
    }
}
