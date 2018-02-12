package model;

import java.util.HashMap;

public class CurrentUser {
    private String userName;
    private HashMap<String, Contact> contactList;
    private HashMap<String, Conversation> conversationList;
    private Profile profile;

    public CurrentUser() {
    }

    public String getUserName() {
        return userName;
    }
    public HashMap<String, Contact> getContactList() {
        return contactList;
    }
    public HashMap<String, Conversation> getConversationList() {
        return conversationList;
    }
    public Profile getProfile() {
        return profile;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setContactList(HashMap<String, Contact> contactList) {
        this.contactList = contactList;
    }
    public void setConversationList(HashMap<String, Conversation> conversationList) {
        this.conversationList = conversationList;
    }
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void clearUser() {
        userName = null;
        contactList = null;
        conversationList = null;
        profile = null;
    }
}
