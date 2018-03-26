package model;

import connection.serverMessages.notificationMessages.*;

import java.text.ParseException;
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
    public void setConversationList(HashMap<String, Conversation> conversationList) { this.conversationList = conversationList; }
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void logIn(NotificationLoggedInMessage message) {
        userName = message.getUsername();
        contactList = message.getContacts();
        conversationList = message.getConversations();
        profile = message.getProfile();
    }

    public void updateUserOnlineStatus(NotificationUserOnlineStatusMessage message) {
        Contact contact = contactList.get(message.getUsername());
        contact.updateOnline(message);
    }

    public void logOut() {
        userName = null;
        contactList = null;
        conversationList = null;
        profile = null;
    }

    public void addContact(NotificationContactAddedMessage message) {
        Contact contact = new Contact(message);
        contactList.put(contact.getUsername(), contact);
    }

    public void removeContact(NotificationContactRemovedMessage message) {
        contactList.remove(message.getUsername());
    }

    public void updateContact(NotificationContactUpdatedMessage message) {
        contactList = message.getContacts();
    }

    public void updateProfile(NotificationProfileUpdatedMessage message) {
        profile = message.getProfile();
    }

    public void addMessage(NotificationMessageReceivedMessage message) throws ParseException {
        if (message.getConversation() == null) { // existing conversation
            Message textMessage = new Message(message);
            Conversation conversation = conversationList.get(message.getConversationKey());
            conversation.addMessage(textMessage);
        } else { // new conversation
            Conversation newConversation = message.getConversation();
            conversationList.put(newConversation.getConversationKey(), newConversation);
        }
    }

    public void updateMessage(NotificationMessageUpdatedMessage message) {
        Conversation conversation = conversationList.get(message.getConversationKey());
        conversation.updateMessage(message);
    }

    public void messageReactions(NotificationMessageReaction message) {
        Conversation conversation = conversationList.get(message.getConversationKey());
        conversation.messageReactions(message);
    }

    public void addUserToConversation(NotificationUserAddedToConversationMessage message) {
        if (message.getConversation() == null) { // conversation already exists
            Conversation conversation = conversationList.get(message.getConversationKey());
            conversation.addUser(message);
        } else { // new conversation
            conversationList.put(message.getConversationKey(), message.getConversation());
        }
    }

    public void removeUserFromConversation(NotificationUserRemovedFromConversationMessage message) {
        if (userName.equals(message.getUsername())) { // user to be removed is current user
            conversationList.remove(message.getConversationKey());
        } else { // user to be removed is not current user
            Conversation conversation = conversationList.get(message.getConversationKey());
            conversation.removeUser(message);
        }
    }

    public void updateMessageRead(NotificationMessageReadMessage message) {
        Conversation conversation = conversationList.get(message.getConversationKey());
        conversation.updateRead(message);
    }

    public void updateMessageTyping(NotificationTypingMessage message) {
        Conversation conversation = conversationList.get(message.getConversationKey());
        conversation.updateTyping(message);
    }
}
