package model;

import connection.serverMessages.notificationMessages.NotificationMessageReaction;
import connection.serverMessages.notificationMessages.NotificationMessageReceivedMessage;
import connection.serverMessages.notificationMessages.NotificationMessageUpdatedMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class Message implements Comparable<Message> {
    /*Time            *time.Time      `json:"time,omitempty"`
    To              *[]string       `json:"to,omitempty"`
    MessageKey      *datastore.Key  `json:"message_key,omitempty"`
    ConversationKey *datastore.Key  `json:"conversation_key,omitempty"`
    From            *string         `json:"from,omitempty"`
    Text            *string         `json:"text,omitempty"`
    reactions       *[]UserReaction `json:"reactions,omitempty"`
    Typing          *bool           `json:"typing,omitempty"`*/

    private String serverTime;
    private String clientTime;
    private ArrayList<String> to;
    private String messageKey;
    private String conversationKey;
    private String from;
    private String text;
    private enum Reaction {  }
    private Map<String, Reactions> reactions;
    private boolean typing;

    public Message() {}

    // Test Constructor
    public Message(String serverTime, String clientTime, ArrayList<String> to, String messageKey, String conversationKey,
                   String from, String text, Map<String, Reactions> reactions, boolean typing) {
        this.serverTime = serverTime;
        this.clientTime = clientTime;
        this.to = to;
        this.messageKey = messageKey;
        this.conversationKey = conversationKey;
        this.from = from;
        this.text = text;
        this.reactions = reactions;
        this.typing = typing;
    }

    // Use this constructor for a new conversation
    public Message(ArrayList<String> to, String text) {
        this.to = to;
        this.text = text;
    }

    // Use this constructor for an existing Conversation
    public Message(String conversationKey, String text) {
        this.conversationKey = conversationKey;
        this.text = text;
    }

    // Use this constructor for reading a message
    public Message(String conversationKey) {
        this.conversationKey = conversationKey;
    }

    public String getClientTime() { return clientTime; }
    public String getServerTime() { return serverTime; }
    public ArrayList<String> getTo() { return to; }
    public String getFrom() { return from; }
    public String getConversationKey() { return conversationKey; }
    public String getMessageKey() { return messageKey; }
    public String getText() { return text; }
    public Map<String, Reactions> getReactions() {
        return reactions;
    }

    public void setText(String text) { this.text = text; }
    public void setClientTime(String clientTime) {
        this.clientTime = clientTime;
    }

    public void updateMessage(NotificationMessageUpdatedMessage message) {
        text = message.getText();
    }

    public void messageReactions(NotificationMessageReaction message) {
        reactions = message.getReactions();
    }

    // Sorts messages by time.
    // If there is a serverTime, that time will be used.  If there is no serverTime, clientTime will be used.
    @Override
    public int compareTo(Message anotherMessage) {
        Date thisMessageDate = null;
        Date anotherMessageDate = null;
        String servertime = this.getServerTime().replace('T',' ').substring(0,23);
        String anotherServertime = anotherMessage.getServerTime().replace('T',' ').substring(0,23);
        SimpleDateFormat sdf = new SimpleDateFormat(Globals.simpldDateFormat);

        if (this.serverTime != null) {
            try {
                thisMessageDate = sdf.parse(servertime);
            } catch (ParseException e) {
                System.out.println("Error converting serverTime to Date while comparing messages: " + e);
            }
        } else {
            try {
                thisMessageDate = sdf.parse(this.clientTime);
            } catch (ParseException e) {
                System.out.println("Error converting clientTime to Date while comparing messages: " + e);
            }
        }

        if (anotherMessage.serverTime != null) {
            try {
                anotherMessageDate = sdf.parse(anotherServertime);
            } catch (ParseException e) {
                System.out.println("Error converting serverTime to Date while comparing messages: " + e);
            }
        } else {
            try {
                anotherMessageDate = sdf.parse(anotherMessage.clientTime);
            } catch (ParseException e) {
                System.out.println("Error converting clientTime to Date while comparing messages: " + e);
            }
        }
        return thisMessageDate.compareTo(anotherMessageDate);
    }
}
