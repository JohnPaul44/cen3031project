import connection.Server;
import connection.ServerConnection;
import connection.serverMessaging.*;
import model.*;
import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionRegisterMessage m = new ActionRegisterMessage("thead9", "bogus,", "Thomas Headley", "thead9@ufl.edu");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogInMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionLogInMessage m = new ActionLogInMessage("thead9", "bogus,");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogOutMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionLogOutMessage m = new ActionLogOutMessage();

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddContactMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionAddContactMessage m = new ActionAddContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveContactMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionRemoveContactMessage m = new ActionRemoveContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendUpdateProfileMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        Profile p = new Profile("Thomas Headley", "thead9", "4074086638");
        ActionUpdateProfileMessage m = new ActionUpdateProfileMessage(p);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSendMessageMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionSendMessageMessage m = new ActionSendMessageMessage(ActionSendMessageMessage.ActionSendMessageMessageType.TO, "suzy", "Hi Suzy!");
        ActionSendMessageMessage m2 = new ActionSendMessageMessage(ActionSendMessageMessage.ActionSendMessageMessageType.CONVERSATIONKEY, "51dcj", "Hi Suzy!");

        conn.getOut().println(m.toJsonString());
        conn.getOut().println(m2.toJsonString());
    }

    @Test
    public void sendUpdateMessageMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionUpdateMessageMessage m = new ActionUpdateMessageMessage("14dv", "8dco", "Hello Suzy");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddUserToConversationMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionAddUserToConversationMessage m = new ActionAddUserToConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveUserFromConversationMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionRemoveUserFromConversationMessage m = new ActionRemoveUserFromConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendReadMessageMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionReadMessageMessage m = new ActionReadMessageMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSetTypingMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionSetTypingMessage m = new ActionSetTypingMessage("thead9", true);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveErrorMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationErrorMessage m = new NotificationErrorMessage(5, "Error #5");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveLoggedInMessage() throws InterruptedException {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationLoggedInMessage m = createLoggedInMessage();

        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void receiveUserOnlineStatusMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationLoggedInMessage loggedInMessage = createLoggedInMessage();
        conn.getOut().println(loggedInMessage.toJsonString());
        NotificationUserOnlineStatusMessage m = new NotificationUserOnlineStatusMessage(true, "suzy");
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(conn.getCurrentUser().getContactList().get("suzy").getOnline());
    }

    @Test
    public void receiveLoggedOutMessage() throws InterruptedException {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationLoggedInMessage loggedInMessage = createLoggedInMessage();
        conn.getOut().println(loggedInMessage.toJsonString());
        NotificationLoggedOutMessage m = new NotificationLoggedOutMessage();
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertNull(conn.getCurrentUser().getUserName());
        assertNull(conn.getCurrentUser().getContactList());
        assertNull(conn.getCurrentUser().getConversationList());
        assertNull(conn.getCurrentUser().getProfile());
    }

    @Test
    public void receiveContactAddedMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationContactAddedMessage m = new NotificationContactAddedMessage("thead9");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveContactRemovedMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationContactRemovedMessage m = new NotificationContactRemovedMessage("thead9");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveProfileUpdatedMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationProfileUpdatedMessage m = new NotificationProfileUpdatedMessage(new Profile("Thomas Headley", "thead9", "4074086638"));
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveMessageReceivedMessage() throws InterruptedException {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        UserReaction u1 = new UserReaction(new int[] {1, 6}, "thead9");
        UserReaction u2 = new UserReaction(new int[] {5, 4}, "suzy");
        NotificationMessageReceivedMessage m = new NotificationMessageReceivedMessage("jhc5", "8cj4", "2018-03-9 03:00:22.012",
                "thead9", "Hello", new UserReaction[] {u1, u2});

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveMessageUpdatedMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationMessageUpdatedMessage m = new NotificationMessageUpdatedMessage("kcn4", "nc4l", "Hello");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveUserAddedToConversationMessage() throws InterruptedException {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

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
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationUserRemovedFromConversationMessage m = new NotificationUserRemovedFromConversationMessage("thead9", "4jv8");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveMessageReadConversation() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationMessageReadMessage m = new NotificationMessageReadMessage("uh4h", "thead9");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveTypingMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

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

    private NotificationLoggedInMessage createLoggedInMessage() {
        Contact c1 = new Contact("thead9", true);
        Contact c2 = new Contact("suzy", false);
        Contact c3 = new Contact("barney", true);
        HashMap<String, Contact> contactList = new HashMap<>();
        contactList.put(c1.getUsername(), c1);
        contactList.put(c2.getUsername(), c2);
        contactList.put(c3.getUsername(), c3);

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

        return m;
    }
}
