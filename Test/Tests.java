import connection.Server;
import connection.ServerConnection;
import connection.serverMessaging.*;
import model.*;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Tests {

    @Test
    public void toJsonStringTest() {
        ActionLogInMessage m = new ActionLogInMessage("thead9", "bogus");

        Gson gson = new Gson();
        String jsonString = gson.toJson(m);

        System.out.println(jsonString);
    }

    @Test
    public void sendRegisterMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionRegisterMessage m = new ActionRegisterMessage("thead9", "bogus,", "Thomas Headley", "thead9@ufl.edu");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogInMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionLogInMessage m = new ActionLogInMessage("thead9", "bogus,");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogOutMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionLogOutMessage m = new ActionLogOutMessage();

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddContactMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionAddContactMessage m = new ActionAddContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveContactMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionRemoveContactMessage m = new ActionRemoveContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendUpdateProfileMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        Profile p = new Profile("Thomas Headley", "thead9", "4074086638");
        ActionUpdateProfileMessage m = new ActionUpdateProfileMessage(p);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSendMessageMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionSendMessageMessage m = new ActionSendMessageMessage(ActionSendMessageMessage.ActionSendMessageMessageType.TO, "suzy", "Hi Suzy!");
        ActionSendMessageMessage m2 = new ActionSendMessageMessage(ActionSendMessageMessage.ActionSendMessageMessageType.CONVERSATIONKEY, "51dcj", "Hi Suzy!");

        conn.getOut().println(m.toJsonString());
        conn.getOut().println(m2.toJsonString());
    }

    @Test
    public void sendUpdateMessageMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionUpdateMessageMessage m = new ActionUpdateMessageMessage("14dv", "8dco", "Hello Suzy");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddUserToConversationMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionAddUserToConversationMessage m = new ActionAddUserToConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveUserFromConversationMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionRemoveUserFromConversationMessage m = new ActionRemoveUserFromConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendReadMessageMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionReadMessageMessage m = new ActionReadMessageMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSetTypingMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        ActionSetTypingMessage m = new ActionSetTypingMessage("thead9", true);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveErrorMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationErrorMessage m = new NotificationErrorMessage(5, "Error #5");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveLoggedInMessage() throws InterruptedException {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        Contact c1 = new Contact("thead9", true);
        Contact c2 = new Contact("suzy", false);
        Contact c3 = new Contact("barney", true);
        ArrayList<Contact> contactList = new ArrayList<>();
        contactList.add(c1);
        contactList.add(c2);
        contactList.add(c3);

        Map<String, Status> memberStatus1 = new HashMap<>();
        memberStatus1.put("thead9", new Status(true, true));
        memberStatus1.put("suzy", new Status(false, false));
        UserReaction ur1 = new UserReaction(new int[] {1, 2}, "thead9");
        UserReaction ur2 = new UserReaction(new int[] {3, 4}, "suzy");
        Message m1 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
                new String[] {"thead9", "suzy"}, "34f", "cn47", "thead9", "hello",
                new UserReaction[] {ur1, ur2}, true);
        Message m2 = new Message("2018-02-8 03:00:21.012", "2018-02-8 03:00:22.012",
                new String[] {"thead9", "suzy"}, "999", "cn47", "thead9", "hello",
                new UserReaction[] {ur1, ur2}, true);
        ArrayList<Message> mList1 = new ArrayList<>();
        mList1.add(m1);
        mList1.add(m2);
        Conversation conv1 = new Conversation("2018-03-9 03:00:22.012", memberStatus1, mList1);

        Map<String, Status> memberStatus2 = new HashMap<>();
        memberStatus2.put("thead9", new Status(true, true));
        memberStatus2.put("barney", new Status(false, false));
        UserReaction ur3 = new UserReaction(new int[] {1, 2}, "thead9");
        UserReaction ur4 = new UserReaction(new int[] {3, 4}, "barney");
        Message m3 = new Message("2018-01-9 03:00:21.012", "2018-01-9 03:00:22.012",
                new String[] {"thead9", "barney"}, "8ch", "nvj4", "thead9", "hi",
                new UserReaction[] {ur3, ur4}, true);
        Message m4 = new Message("2018-01-8 03:00:21.012", "2018-01-8 03:00:22.012",
                new String[] {"thead9", "suzy"}, "888", "nvj4", "thead9", "hi",
                new UserReaction[] {ur3, ur4}, true);
        ArrayList<Message> mList2 = new ArrayList<>();
        mList2.add(m3);
        mList2.add(m4);
        Conversation conv2 = new Conversation("2018-03-9 03:00:22.012", memberStatus2, mList2);

        ArrayList<Conversation> cList = new ArrayList<>();
        cList.add(conv1);
        cList.add(conv2);

        NotificationLoggedInMessage m = new NotificationLoggedInMessage("thead9",
                new Profile("Thomas Headley", "thead9@ufl.edu", "4074086638"), contactList, cList);

        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void receiveUserOnlineStatusMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationUserOnlineStatusMessage m = new NotificationUserOnlineStatusMessage(true, "thead9");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveLoggedOutMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationLoggedOutMessage m = new NotificationLoggedOutMessage();
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveContactAddedMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationContactAddedMessage m = new NotificationContactAddedMessage("thead9");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveContactRemovedMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationContactRemovedMessage m = new NotificationContactRemovedMessage("thead9");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveProfileUpdatedMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationProfileUpdatedMessage m = new NotificationProfileUpdatedMessage(new Profile("Thomas Headley", "thead9", "4074086638"));
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveMessageReceivedMessage() throws InterruptedException {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        UserReaction u1 = new UserReaction(new int[] {1, 6}, "thead9");
        UserReaction u2 = new UserReaction(new int[] {5, 4}, "suzy");
        NotificationMessageReceivedMessage m = new NotificationMessageReceivedMessage("jhc5", "8cj4", "2018-03-9 03:00:22.012",
                "thead9", "Hello", new UserReaction[] {u1, u2});

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveMessageUpdatedMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationMessageUpdatedMessage m = new NotificationMessageUpdatedMessage("kcn4", "nc4l", "Hello");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveUserAddedToConversationMessage() throws InterruptedException {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        Map<String, Status> memberStatus1 = new HashMap<>();
        memberStatus1.put("thead9", new Status(true, true));
        memberStatus1.put("suzy", new Status(false, false));
        UserReaction ur1 = new UserReaction(new int[] {1, 2}, "thead9");
        UserReaction ur2 = new UserReaction(new int[] {3, 4}, "suzy");
        Message m1 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
                new String[] {"thead9", "suzy"}, "34f", "cn47", "thead9", "hello",
                new UserReaction[] {ur1, ur2}, true);
        Message m2 = new Message("2018-02-8 03:00:21.012", "2018-02-8 03:00:22.012",
                new String[] {"thead9", "suzy"}, "999", "cn47", "thead9", "hello",
                new UserReaction[] {ur1, ur2}, true);
        ArrayList<Message> mList1 = new ArrayList<>();
        mList1.add(m1);
        mList1.add(m2);
        Conversation conv1 = new Conversation("2018-03-9 03:00:22.012", memberStatus1, mList1);

        NotificationUserAddedToConversationMessage m = new NotificationUserAddedToConversationMessage("thead9", "4hyd", conv1);
        conn.getOut().println(m.toJsonString());

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void receiveUserRemovedFromConversation() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationUserRemovedFromConversationMessage m = new NotificationUserRemovedFromConversationMessage("thead9", "4jv8");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveMessageReadConversation() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationMessageReadMessage m = new NotificationMessageReadMessage("uh4h", "thead9");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveTypingMessage() {
        LoggedInUser loggedInUser = new LoggedInUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer(loggedInUser);

        NotificationTypingMessage m = new NotificationTypingMessage("kj4n", "thead9", true);
        conn.getOut().println(m.toJsonString());
    }

    private void startTestServerEcho(){
        Thread thread = new Thread(() -> {
            try {
                Server s = new Server();
                s.startServerEcho();
            } catch (IOException e) {
                System.out.println("Error starting server in test: " + e);
            }
        });
        thread.start();
    }
}
