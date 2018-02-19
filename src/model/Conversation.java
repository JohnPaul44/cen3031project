package model;

import connection.serverMessages.NotificationMessageReceivedMessage;

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

    // Test Constructor
    public Conversation(String conversationKey, String time, Map<String, Status> memberStatus, HashMap<String, Message> messages) {
        this.conversationKey = conversationKey;
        this.time = time;
        this.memberStatus = memberStatus;
        this.messages = messages;
    }

    public Conversation(NotificationMessageReceivedMessage messageFromServer) { // used when message received from server is first in a conversation
        this.conversationKey = messageFromServer.getConversationKey();
        this.time = messageFromServer.getServerTime();
        this.memberStatus = new HashMap<>();
        memberStatus.put(messageFromServer.getFrom(), new Status(true, false));
        // TODO group message for first message status
        this.messages = new HashMap<>();
        messages.put(messageFromServer.getConversationKey(), new Message(messageFromServer));
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

    public void addMessageToConversation(Message messageToAdd) throws ParseException {
        messages.put(messageToAdd.getConversationKey(), messageToAdd);
        Date currentConversationTime = new SimpleDateFormat(Globals.simpldDateFormat).parse(time);
        Date messageToAddTime = new SimpleDateFormat(Globals.simpldDateFormat).parse(messageToAdd.getServerTime());
        if (messageToAddTime.after(currentConversationTime)) {
            time = messageToAdd.getServerTime();
        }
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

