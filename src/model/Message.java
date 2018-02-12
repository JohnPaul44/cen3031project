package model;

import connection.serverMessaging.NotificationMessageReceivedMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Comparable<Message> {
    /*Time            *time.Time      `json:"time,omitempty"`
    To              *[]string       `json:"to,omitempty"`
    MessageKey      *datastore.Key  `json:"message_key,omitempty"`
    ConversationKey *datastore.Key  `json:"conversation_key,omitempty"`
    From            *string         `json:"from,omitempty"`
    Text            *string         `json:"text,omitempty"`
    Reactions       *[]UserReaction `json:"reactions,omitempty"`
    Typing          *bool           `json:"typing,omitempty"`*/

    private String serverTime;
    private String clientTime;
    private String[] to;
    private String messageKey;
    private String conversationKey;
    private String from;
    private String text;
    private enum Reaction {  }
    private UserReaction[] userReactions;
    private boolean typing;

    // Test Constructor
    public Message(String serverTime, String clientTime, String[] to, String messageKey, String conversationKey,
                   String from, String text, UserReaction[] userReactions, boolean typing) {
        this.serverTime = serverTime;
        this.clientTime = clientTime;
        this.to = to;
        this.messageKey = messageKey;
        this.conversationKey = conversationKey;
        this.from = from;
        this.text = text;
        this.userReactions = userReactions;
        this.typing = typing;
    }

    public Message(NotificationMessageReceivedMessage messageFromServer) {
        this.conversationKey = messageFromServer.getConversationKey();
        this.messageKey = messageFromServer.getMessageKey();
        this.serverTime = messageFromServer.getServerTime();
        this.from = messageFromServer.getFrom();
        this.text = messageFromServer.getText();
        if (messageFromServer.getReactions() != null) {
            this.userReactions = messageFromServer.getReactions();
        }
    }

    // New Conversation
    public Message(String[] to, String text, String clientTime) {
        this.to = to;
        this.text = text;
        this.clientTime = clientTime;
    }

    // Existing Conversation
    public Message(String conversationKey, String text, String clientTime) {
        this.conversationKey = conversationKey;
        this.text = text;
        this.clientTime = clientTime;
    }

    public String getClientTime() { return clientTime; }
    public String getServerTime() { return serverTime; }
    public String[] getTo() { return to; }
    public String getFrom() { return from; }
    public String getConversationKey() { return conversationKey; }

    // Sorts messages by time.
    // If there is a serverTime, that time will be used.  If there is no serverTime, clientTime will be used.
    @Override
    public int compareTo(Message anotherMessage) {
        Date thisMessageDate = null;
        Date anotherMessageDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat(Globals.simpldDateFormat);

        if (this.serverTime != null) {
            try {
                thisMessageDate = sdf.parse(this.serverTime);
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
                thisMessageDate = sdf.parse(anotherMessage.serverTime);
            } catch (ParseException e) {
                System.out.println("Error converting serverTime to Date while comparing messages: " + e);
            }
        } else {
            try {
                thisMessageDate = sdf.parse(anotherMessage.clientTime);
            } catch (ParseException e) {
                System.out.println("Error converting clientTime to Date while comparing messages: " + e);
            }
        }
        return anotherMessageDate.compareTo(thisMessageDate);
    }
}
