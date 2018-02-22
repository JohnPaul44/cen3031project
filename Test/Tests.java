import connection.Server;
import connection.ServerConnection;
import connection.serverMessages.*;
import model.*;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Tests {

    // TODO create test for sorting messages
    // TODO create test for sorting conversations

    @Test
    public void toJsonStringTest() {
        ActionLogInMessage m = new ActionLogInMessage("thead9", "bogus");

        Gson gson = new Gson();
        String jsonString = gson.toJson(m);

        System.out.println(jsonString);
    }

    @Test
    public void sendRegisterMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionRegisterMessage m = new ActionRegisterMessage("thead9", "bogus,", "Thomas Headley", "thead9@ufl.edu");
        conn.sendMessageToServer(m);
        TimeUnit.SECONDS.sleep(4);
    }

    @Test
    public void sendLogInMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionLogInMessage m = new ActionLogInMessage("thead9", "bogus,");
        conn.sendMessageToServer(m);
        TimeUnit.SECONDS.sleep(4);
    }

    @Test
    public void sendLogOutMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionLogOutMessage m = new ActionLogOutMessage();

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddContactMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionAddContactMessage m = new ActionAddContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveContactMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionRemoveContactMessage m = new ActionRemoveContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendUpdateProfileMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        Profile p = new Profile("Thomas Headley", "thead9", "4074086638");
        ActionUpdateProfileMessage m = new ActionUpdateProfileMessage(p);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSendMessageMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

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
        conn.listenToServer();

        ActionUpdateMessageMessage m = new ActionUpdateMessageMessage("14dv", "8dco", "Hello Suzy");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddUserToConversationMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionAddUserToConversationMessage m = new ActionAddUserToConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveUserFromConversationMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionRemoveUserFromConversationMessage m = new ActionRemoveUserFromConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendReadMessageMessage() {
        CurrentUser currentUser = new CurrentUser();

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionReadMessageMessage m = new ActionReadMessageMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSetTypingMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        ActionSetTypingMessage m = new ActionSetTypingMessage("thead9", true);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveErrorMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationErrorMessage m = new NotificationErrorMessage(5, "Error #5");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveLoggedInMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage m = createLoggedInMessage();

        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void receiveUserOnlineStatusMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage loggedInMessage = createLoggedInMessage();
        conn.getOut().println(loggedInMessage.toJsonString());
        NotificationUserOnlineStatusMessage m = new NotificationUserOnlineStatusMessage(true, "suzy");
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(conn.getCurrentUser().getContactList().get("suzy").getOnline());
    }

    @Test
    public void receiveLoggedOutMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

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
    public void receiveContactAddedMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationContactAddedMessage m = new NotificationContactAddedMessage("fred");
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(conn.getCurrentUser().getContactList().get("fred").getUsername().equals("fred"));
        assertFalse(conn.getCurrentUser().getContactList().get("fred").getOnline());
    }

    @Test
    public void receiveContactRemovedMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationContactRemovedMessage m = new NotificationContactRemovedMessage("suzy");
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertNull(conn.getCurrentUser().getContactList().get("suzy"));
    }

    @Test
    public void receiveProfileUpdatedMessage() throws InterruptedException {

        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationProfileUpdatedMessage m = new NotificationProfileUpdatedMessage(new Profile("Thomas Headley", "thead9@ufl.edu", "4074086638"));
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(conn.getCurrentUser().getProfile().getName().equals("Thomas Headley"));
        assertTrue(conn.getCurrentUser().getProfile().getEmail().equals("thead9@ufl.edu"));
        assertTrue(conn.getCurrentUser().getProfile().getPhone().equals("4074086638"));
    }

    @Test
    public void receiveMessageReceivedForExistingConversationMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        UserReaction u1 = new UserReaction(new int[] {1, 6}, "thead9");
        UserReaction u2 = new UserReaction(new int[] {5, 4}, "suzy");

        // without user reactions
        //NotificationMessageReceivedMessage m = new NotificationMessageReceivedMessage("conv3", "8cj4", "2018-03-9 03:00:22.012",
        //       "thead9", "Hello");
        NotificationMessageReceivedMessage m = new NotificationMessageReceivedMessage("conv1", "8cj4", "2018-03-9 03:00:22.012",
                "thead9", "Hello", new UserReaction[] {u1, u2});

        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(conn.getCurrentUser().getConversationList().get("conv1").getTime().equals("2018-03-9 03:00:22.012"));
    }

    @Test
    public void receiveMessageReceivedForNewConversationMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());

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
        HashMap<String, Message> mList2 = new HashMap<>();
        mList2.put(m3.getConversationKey(), m3);
        mList2.put(m4.getConversationKey(), m4);
        Conversation conv3 = new Conversation("conv3", "2017-03-9 03:00:22.012", memberStatus2, mList2);

        NotificationMessageReceivedMessage m = new NotificationMessageReceivedMessage(conv3);

        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(conn.getCurrentUser().getConversationList().get("conv3").getTime().equals("2017-03-9 03:00:22.012"));
    }

    @Test
    public void receiveMessageUpdatedMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage m = createLoggedInMessage();
        conn.getOut().println(m.toJsonString());
        NotificationMessageUpdatedMessage message = new NotificationMessageUpdatedMessage("conv2", "m3", "whats up");
        conn.getOut().println(message.toJsonString());
        TimeUnit.SECONDS.sleep(4);
        assertTrue(conn.getCurrentUser().getConversationList().get("conv2").getMessages().get("m3").getText().equals("whats up"));

    }

    @Test
    public void receiveUserAddedToConversationMessageExisting() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationUserAddedToConversationMessage m = new NotificationUserAddedToConversationMessage("fred", "conv1");
        conn.getOut().println(m.toJsonString());

        TimeUnit.SECONDS.sleep(5);
        assertFalse(conn.getCurrentUser().getConversationList().get("conv1").getMemberStatus().get("fred").getRead());
    }

    @Test
    public void receiveUserAddedToConversationMessageNonExisting() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());

        Map<String, Status> memberStatus1 = new HashMap<>();
        memberStatus1.put("thead9", new Status(true, true));
        memberStatus1.put("fred", new Status(false, false));
        UserReaction ur1 = new UserReaction(new int[] {1, 2}, "thead9");
        UserReaction ur2 = new UserReaction(new int[] {3, 4}, "suzy");
        Message m1 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
                new String[] {"thead9", "suzy"}, "34f", "cn47", "thead9", "hello",
                new UserReaction[] {ur1, ur2}, true);
        Message m2 = new Message("2018-02-8 03:00:21.012", "2018-02-8 03:00:22.012",
                new String[] {"thead9", "suzy"}, "999", "cn47", "thead9", "hello",
                new UserReaction[] {ur1, ur2}, true);
        HashMap<String, Message> mList1 = new HashMap<>();
        mList1.put(m1.getConversationKey(), m1);
        mList1.put(m2.getConversationKey(), m2);
        Conversation conv3 = new Conversation("conv3", "2018-03-9 03:00:22.012", memberStatus1, mList1);

        NotificationUserAddedToConversationMessage m = new NotificationUserAddedToConversationMessage(conv3);
        conn.getOut().println(m.toJsonString());

        TimeUnit.SECONDS.sleep(5);
        conn.getCurrentUser();
        assertFalse(conn.getCurrentUser().getConversationList().get("conv3").getMemberStatus().get("fred").getRead());

    }

    @Test
    public void receiveUserRemovedFromConversationSelf() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationUserRemovedFromConversationMessage m = new NotificationUserRemovedFromConversationMessage("thead9", "conv2");
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(5);
        assertNull(conn.getCurrentUser().getConversationList().get("conv2"));
    }

    @Test
    public void receiveUserRemovedFromConversationNotSelf() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationUserRemovedFromConversationMessage m = new NotificationUserRemovedFromConversationMessage("barney", "conv2");
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(5);
        assertNull(conn.getCurrentUser().getConversationList().get("conv2").getMemberStatus().get("barney"));
    }

    @Test
    public void receiveMessageReadConversation() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationMessageReadMessage m = new NotificationMessageReadMessage("conv2", "barney");
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(5);
        assertTrue(conn.getCurrentUser().getConversationList().get("conv2").getMemberStatus().get("barney").getRead());
    }

    @Test
    public void receiveTypingMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.listenToServer();

        NotificationLoggedInMessage message = createLoggedInMessage();
        conn.getOut().println(message.toJsonString());
        NotificationTypingMessage m = new NotificationTypingMessage("conv2", "barney", true);
        conn.getOut().println(m.toJsonString());
        TimeUnit.SECONDS.sleep(5);
        assertTrue(conn.getCurrentUser().getConversationList().get("conv2").getMemberStatus().get("barney").getTyping());
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
        HashMap<String, Message> mList1 = new HashMap<>();
        mList1.put(m1.getMessageKey(), m1);
        mList1.put(m2.getMessageKey(), m2);
        Conversation conv1 = new Conversation("conv1", "2018-03-9 03:00:22.012", memberStatus1, mList1);

        Map<String, Status> memberStatus2 = new HashMap<>();
        memberStatus2.put("thead9", new Status(true, true));
        memberStatus2.put("barney", new Status(false, false));
        UserReaction ur3 = new UserReaction(new int[] {1, 2}, "thead9");
        UserReaction ur4 = new UserReaction(new int[] {3, 4}, "barney");
        Message m3 = new Message("2018-01-9 03:00:21.012", "2018-01-9 03:00:22.012",
                new String[] {"thead9", "barney"}, "m3", "nvj4", "thead9", "hi",
                new UserReaction[] {ur3, ur4}, true);
        Message m4 = new Message("2018-01-8 03:00:21.012", "2018-01-8 03:00:22.012",
                new String[] {"thead9", "suzy"}, "m4", "nvj4", "thead9", "hi",
                new UserReaction[] {ur3, ur4}, true);
        HashMap<String, Message> mList2 = new HashMap<>();
        mList2.put(m3.getMessageKey(), m3);
        mList2.put(m4.getMessageKey(), m4);
        Conversation conv2 = new Conversation("conv2", "2017-03-9 03:00:22.012", memberStatus2, mList2);

        HashMap<String, Conversation> cList = new HashMap<>();
        cList.put(conv1.getConversationKey(), conv1);
        cList.put(conv2.getConversationKey(), conv2);

        NotificationLoggedInMessage m = new NotificationLoggedInMessage("thead9",
                new Profile("Thomas Headley", "thead9@ufl.edu", "4074086638"), contactList, cList);

        return m;
    }
}
