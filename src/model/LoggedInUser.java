package model;

import java.util.ArrayList;

public class LoggedInUser {
    ArrayList<Contact> contactList;
    ArrayList<Conversation> conversationList;
    Profile profile;

    public LoggedInUser() {
    }

    public ArrayList<Contact> getContactList() {
        return contactList;
    }
    public ArrayList<Conversation> getConversationList() {
        return conversationList;
    }
    public Profile getProfile() {
        return profile;
    }

    public void setContactList(ArrayList<Contact> contactList) {
        this.contactList = contactList;
    }
    public void setConversationList(ArrayList<Conversation> conversationList) {
        this.conversationList = conversationList;
    }
    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
