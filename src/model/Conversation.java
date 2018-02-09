package model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Conversation implements Comparable<Conversation> {
    /*Time     time.Time         `json:"time"`
    MemberStatus  map[string]Status `json:"member_status"`
    Messages []Message         `json:"messages"`*/

    private String time;
    private Map<String, Status> memberStatus;
    private ArrayList<Message> messages;

    public Conversation(Message firstMessage) {

        Map<String, Status> memberStatusMap = new HashMap<>();
        memberStatusMap.put(firstMessage.getFrom(), new Status(true, false));
        for (String user : firstMessage.getTo()) {
            memberStatusMap.put(user, new Status(false, false));
        }

        ArrayList<Message> messagesList = new ArrayList<Message>();
        messagesList.add(firstMessage);

        this.time = firstMessage.getClientTime();
        this.memberStatus = memberStatusMap;
        this.messages = messagesList;

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

