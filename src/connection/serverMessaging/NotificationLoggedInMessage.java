package connection.serverMessaging;

import model.Contact;
import model.Conversation;
import model.Profile;

import java.util.ArrayList;

public class NotificationLoggedInMessage extends ServerMessage {
    private String username;
    private Profile profile;
    private ArrayList<Contact> contacts;
    private ArrayList<Conversation> conversations;

    public NotificationLoggedInMessage(String username, Profile profile, ArrayList<Contact> contacts, ArrayList<Conversation> conversations) {
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
    public ArrayList<Contact> getContacts() {
        return contacts;
    }
    public ArrayList<Conversation> getConversations() {
        return conversations;
    }
}
