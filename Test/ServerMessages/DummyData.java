package ServerMessages;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class DummyData {

    public String username1 = "thead9";
    public String password1 = "bogus";
    public String firstName1 = "Thomas";
    public String lastName1 = "Headley";
    public String email1 = "thead9@ufl.edu";
    public String phone1 = "4074086638";
    public Profile.Gender gender1 = Profile.Gender.MALE;
    public String birthday1 = "06/14/1995";
    public String securityQuestion1 = "Who are you?";
    public String securityAnswer1 = "Me";
    public String color = "0xb399ffff";
    public Contact contact1;
    public Map<String, Reactions> reactions1 = new HashMap<>();
    public Reactions specificReactions1 = new Reactions(new int[]{1, 2}, username1);

    public FriendshipStats friendshipStats1 = new FriendshipStats(3,2,1);

    public String username2 = "User 2";
    public Contact contact2;
    public Map<String, Reactions> reactions2 = new HashMap<>();
    public Reactions specificReactions2 = new Reactions(new int[]{1, 2}, username2);

    public String username3 = "User 3";
    public Contact contact3;
    public Map<String, Reactions> reactions3 = new HashMap<>();
    public Reactions specificReactions3 = new Reactions(new int[]{1, 2}, username3);

    public Profile profile;

    public String conversationKey1 = "conversationKey1";
    public String conversationKey2 = "conversationKey2";

    public String messageText = "Message Text";
    public Message message1;
    public String messageKey1 = "messageKey1";
    public Message message2;
    public String messageKey2 = "messageKey2";
    public Message message3;
    public String messageKey3 = "messageKey3";
    public Message message4;
    public String messageKey4 = "messageKey4";

    public int errorNumber = 1;
    public String errorMessage = "Error Message";

    DummyData() {
        profile = new Profile(firstName1, lastName1, email1, phone1, gender1, birthday1, color);
        setUpReactions();
        setUpContacts();
        setUpMessages();
    }

    private void setUpMessages() {
        ArrayList<String> to = new ArrayList<>();
        to.add(username1);
        message1 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
                to, messageKey1, conversationKey1, username2, messageText,
                reactions1, true);
        message2 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
                to, messageKey2, conversationKey1, username2, messageText,
                reactions1, true);
        message3 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
                to, messageKey3, conversationKey2, username3, messageText,
                reactions1, true);
        message4 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
                to, messageKey4, conversationKey2, username3, messageText,
                reactions1, true);
    }

    private void setUpContacts() {
        contact1 = new Contact(username1, true);
        contact2 = new Contact(username2, false);
        contact3 = new Contact(username3, true);
    }

    private void setUpReactions() {
        reactions1.put(username1, specificReactions1);
        reactions2.put(username2, specificReactions2);
        reactions3.put(username3, specificReactions3);
    }
}
