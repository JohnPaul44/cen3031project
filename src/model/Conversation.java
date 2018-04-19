package model;

import connection.serverMessages.notificationMessages.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Conversation implements Comparable<Conversation> {
    /*Time     time.Time         `json:"time"`
    MemberStatus  map[string]Status `json:"member_status"`
    Messages []Message         `json:"messages"`*/

    private String conversationKey;
    private String time;
    private Map<String, Status> memberStatus;
    private HashMap<String, Message> messages;

    public Conversation() {}

    // Test Constructor
    public Conversation(String conversationKey, String time, Map<String, Status> memberStatus, HashMap<String, Message> messages) {
        this.conversationKey = conversationKey;
        this.time = time;
        this.memberStatus = memberStatus;
        this.messages = messages;
    }

    public Conversation(Message firstMessage) {
        Map<String, Status> memberStatusMap = new HashMap<>();
        memberStatusMap.put(firstMessage.getFrom(), new Status(true, false));
        for (String user : firstMessage.getTo()) {
            memberStatusMap.put(user, new Status(false, false));
        }

        HashMap<String, Message> messagesList = new HashMap<>();
        messagesList.put(firstMessage.getConversationKey(), firstMessage);

        this.time = firstMessage.getClientTime();
        this.memberStatus = memberStatusMap;
        this.messages = messagesList;

    }

    public Map<String, Status> getMemberStatus() {
        return memberStatus;
    }
    public String getConversationKey() { return conversationKey; }
    public String getTime() { return time; }
    public HashMap<String, Message> getMessages() { return messages; }

    public void addMessage(Message message) throws ParseException {

        messages.put(message.getMessageKey(), message);
//        if(time!=null) {
//            Date currentConversationTime = new SimpleDateFormat(Globals.simpldDateFormat).parse(time);
//            Date messageToAddTime = new SimpleDateFormat(Globals.simpldDateFormat).parse(message.getServerTime());
//            if (messageToAddTime.after(currentConversationTime)) {
//                time = message.getServerTime();
//            }
//        }
        time = message.getServerTime();
    }

    public void updateMessage(NotificationMessageUpdatedMessage notificationMessageUpdatedMessageMessage) {
        Message message = messages.get(notificationMessageUpdatedMessageMessage.getMessageKey());
        message.updateMessage(notificationMessageUpdatedMessageMessage);
    }

    public void messageReactions(NotificationMessageReaction notificationMessageReactionMessage) {
        Message message = messages.get(notificationMessageReactionMessage.getMessageKey());
        message.messageReactions(notificationMessageReactionMessage);
    }

    public void addUser(NotificationUserAddedToConversationMessage message) {
        memberStatus.put(message.getUsername(), new Status());
    }

    public void removeUser(NotificationUserRemovedFromConversationMessage message) {
        memberStatus.remove(message.getUsername());
    }

    public void updateRead(NotificationMessageReadMessage message) {
        Status status = memberStatus.get(message.getFrom());
        status.updateRead(message);
    }

    public void updateTyping(NotificationTypingMessage message) {
        Status status = memberStatus.get(message.getMessage().getFrom());
        status.updateTyping(message);
    }

    @Override
    public int compareTo(Conversation anotherConversation) {
        Date thisConversationDate = null;
        Date anotherConversationDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat(Globals.simpldDateFormat);

        try {
            thisConversationDate = sdf.parse(this.time);
            anotherConversationDate = sdf.parse(anotherConversation.time);
        } catch (ParseException e) {
            System.out.println("Error converting time to Date while comparing Conversations");
        }

        return anotherConversationDate.compareTo(thisConversationDate);
    }
}

