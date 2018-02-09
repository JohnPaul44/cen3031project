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
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionRegisterMessage m = new ActionRegisterMessage("thead9", "bogus,", "Thomas Headley", "thead9@ufl.edu");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogInMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionLogInMessage m = new ActionLogInMessage("thead9", "bogus,");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogOutMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionLogOutMessage m = new ActionLogOutMessage();

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddContactMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionAddContactMessage m = new ActionAddContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveContactMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionRemoveContactMessage m = new ActionRemoveContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendUpdateProfileMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        Profile p = new Profile("Thomas Headley", "thead9", "4074086638");
        ActionUpdateProfileMessage m = new ActionUpdateProfileMessage(p);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSendMessageMessage() {
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
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionUpdateMessageMessage m = new ActionUpdateMessageMessage("14dv", "8dco", "Hello Suzy");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddUserToConversationMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionAddUserToConversationMessage m = new ActionAddUserToConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveUserFromConversationMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionRemoveUserFromConversationMessage m = new ActionRemoveUserFromConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendReadMessageMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionReadMessageMessage m = new ActionReadMessageMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSetTypingMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        ActionSetTypingMessage m = new ActionSetTypingMessage("thead9", true);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveErrorMessage() {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

        NotificationErrorMessage m = new NotificationErrorMessage(5, "Error #5");
        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void receiveLoggedInMessage() throws InterruptedException {
        startTestServerEcho();

        ServerConnection conn = new ServerConnection();
        conn.startListeningToServer();

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
