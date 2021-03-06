import connection.ServerConnectionTestDouble;
import connection.ServerTestDouble;
import connection.ServerConnection;
import connection.serverMessages.*;
import model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Tests {

    // TODO create test for sorting messages
    // TODO create test for sorting conversations










//
//    @Test
//    public void receiveMessageReceivedForNewConversationMessage() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage message = createLoggedInMessage();
//        conn.getOut().println(message.toJsonString());
//
//        Map<String, Status> memberStatus2 = new HashMap<>();
//        memberStatus2.put("thead9", new Status(true, true));
//        memberStatus2.put("barney", new Status(false, false));
//        Reactions ur3 = new Reactions(new int[] {1, 2}, "thead9");
//        Reactions ur4 = new Reactions(new int[] {3, 4}, "barney");
//        Map<String, Reactions> reactions1 = new HashMap<>();
//        reactions1.put("thead9", ur3);
//        reactions1.put("barney", ur4);
//        Message m3 = new Message("2018-01-9 03:00:21.012", "2018-01-9 03:00:22.012",
//                new String[] {"thead9", "barney"}, "8ch", "nvj4", "thead9", "hi",
//                reactions1, true);
//        Message m4 = new Message("2018-01-8 03:00:21.012", "2018-01-8 03:00:22.012",
//                new String[] {"thead9", "suzy"}, "888", "nvj4", "thead9", "hi",
//                reactions1, true);
//        HashMap<String, Message> mList2 = new HashMap<>();
//        mList2.put(m3.getConversationKey(), m3);
//        mList2.put(m4.getConversationKey(), m4);
//        Conversation conv3 = new Conversation("conv3", "2017-03-9 03:00:22.012", memberStatus2, mList2);
//
//        NotificationMessageReceivedMessage m = new NotificationMessageReceivedMessage(conv3);
//
//        conn.getOut().println(m.toJsonString());
//        TimeUnit.SECONDS.sleep(4);
//        assertTrue(conn.getCurrentUser().getConversationList().get("conv3").getTime().equals("2017-03-9 03:00:22.012"));
//    }
//
//    @Test
//    public void receiveMessageUpdatedMessage() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage m = createLoggedInMessage();
//        conn.getOut().println(m.toJsonString());
//        NotificationMessageUpdatedMessage message = new NotificationMessageUpdatedMessage("conv2", "m3", "whats up");
//        conn.getOut().println(message.toJsonString());
//        TimeUnit.SECONDS.sleep(4);
//        assertTrue(conn.getCurrentUser().getConversationList().get("conv2").getMessages().get("m3").getText().equals("whats up"));
//
//    }
//
//    @Test
//    public void receiveUserAddedToConversationMessageExisting() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage message = createLoggedInMessage();
//        conn.getOut().println(message.toJsonString());
//        NotificationUserAddedToConversationMessage m = new NotificationUserAddedToConversationMessage("fred", "conv1");
//        conn.getOut().println(m.toJsonString());
//
//        TimeUnit.SECONDS.sleep(5);
//        assertFalse(conn.getCurrentUser().getConversationList().get("conv1").getMemberStatus().get("fred").getRead());
//    }
//
//    @Test
//    public void receiveUserAddedToConversationMessageNonExisting() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage message = createLoggedInMessage();
//        conn.getOut().println(message.toJsonString());
//
//        Map<String, Status> memberStatus1 = new HashMap<>();
//        memberStatus1.put("thead9", new Status(true, true));
//        memberStatus1.put("fred", new Status(false, false));
//        Reactions ur1 = new Reactions(new int[] {1, 2}, "thead9");
//        Reactions ur2 = new Reactions(new int[] {3, 4}, "suzy");
//        Map<String, Reactions> reactions1 = new HashMap<>();
//        reactions1.put("thead9", ur1);
//        reactions1.put("suzy", ur2);
//        Message m1 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
//                new String[] {"thead9", "suzy"}, "34f", "cn47", "thead9", "hello",
//                reactions1, true);
//        Message m2 = new Message("2018-02-8 03:00:21.012", "2018-02-8 03:00:22.012",
//                new String[] {"thead9", "suzy"}, "999", "cn47", "thead9", "hello",
//                reactions1, true);
//        HashMap<String, Message> mList1 = new HashMap<>();
//        mList1.put(m1.getConversationKey(), m1);
//        mList1.put(m2.getConversationKey(), m2);
//        Conversation conv3 = new Conversation("conv3", "2018-03-9 03:00:22.012", memberStatus1, mList1);
//
//        NotificationUserAddedToConversationMessage m = new NotificationUserAddedToConversationMessage(conv3);
//        conn.getOut().println(m.toJsonString());
//
//        TimeUnit.SECONDS.sleep(5);
//        conn.getCurrentUser();
//        assertFalse(conn.getCurrentUser().getConversationList().get("conv3").getMemberStatus().get("fred").getRead());
//
//    }
//
//    @Test
//    public void receiveUserRemovedFromConversationSelf() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage message = createLoggedInMessage();
//        conn.getOut().println(message.toJsonString());
//        NotificationUserRemovedFromConversationMessage m = new NotificationUserRemovedFromConversationMessage("thead9", "conv2");
//        conn.getOut().println(m.toJsonString());
//        TimeUnit.SECONDS.sleep(5);
//        assertNull(conn.getCurrentUser().getConversationList().get("conv2"));
//    }
//
//    @Test
//    public void receiveUserRemovedFromConversationNotSelf() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage message = createLoggedInMessage();
//        conn.getOut().println(message.toJsonString());
//        NotificationUserRemovedFromConversationMessage m = new NotificationUserRemovedFromConversationMessage("barney", "conv2");
//        conn.getOut().println(m.toJsonString());
//        TimeUnit.SECONDS.sleep(5);
//        assertNull(conn.getCurrentUser().getConversationList().get("conv2").getMemberStatus().get("barney"));
//    }
//
//    @Test
//    public void receiveMessageReadConversation() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage message = createLoggedInMessage();
//        conn.getOut().println(message.toJsonString());
//        NotificationMessageReadMessage m = new NotificationMessageReadMessage("conv2", "barney");
//        conn.getOut().println(m.toJsonString());
//        TimeUnit.SECONDS.sleep(5);
//        assertTrue(conn.getCurrentUser().getConversationList().get("conv2").getMemberStatus().get("barney").getRead());
//    }
//
//    @Test
//    public void receiveTypingMessage() throws InterruptedException {
//        startTestServerEcho();
//
//        ServerConnection conn = new ServerConnection();
//        conn.listenToServer();
//
//        NotificationLoggedInMessage message = createLoggedInMessage();
//        conn.getOut().println(message.toJsonString());
//        NotificationTypingMessage m = new NotificationTypingMessage("conv2", "barney", true);
//        conn.getOut().println(m.toJsonString());
//        TimeUnit.SECONDS.sleep(5);
//        assertTrue(conn.getCurrentUser().getConversationList().get("conv2").getMemberStatus().get("barney").getTyping());
//    }
//
//    private void startTestServerEcho(){
//        /*Thread thread = new Thread(() -> {
//            try {
//                ServerTestDouble s = new ServerTestDouble();
//                //s.startServerEcho();
//            } catch (IOException e) {
//                System.out.println("Error starting server in test: " + e);
//            }
//        });
//        thread.start();*/
//    }
//
//    private NotificationLoggedInMessage createLoggedInMessage() {
//        Contact c1 = new Contact("thead9", true);
//        Contact c2 = new Contact("suzy", false);
//        Contact c3 = new Contact("barney", true);
//        HashMap<String, Contact> contactList = new HashMap<>();
//        contactList.put(c1.getUsername(), c1);
//        contactList.put(c2.getUsername(), c2);
//        contactList.put(c3.getUsername(), c3);
//
//        Map<String, Status> memberStatus1 = new HashMap<>();
//        memberStatus1.put("thead9", new Status(true, true));
//        memberStatus1.put("suzy", new Status(false, false));
//        Reactions ur1 = new Reactions(new int[] {1, 2}, "thead9");
//        Reactions ur2 = new Reactions(new int[] {3, 4}, "suzy");
//        Map<String, Reactions> reactions1 = new HashMap<>();
//        reactions1.put("thead9", ur1);
//        reactions1.put("suzy", ur2);
//        Message m1 = new Message("2018-02-9 03:00:21.012", "2018-02-9 03:00:22.012",
//                new String[] {"thead9", "suzy"}, "34f", "cn47", "thead9", "hello",
//                reactions1, true);
//        Message m2 = new Message("2018-02-8 03:00:21.012", "2018-02-8 03:00:22.012",
//                new String[] {"thead9", "suzy"}, "999", "cn47", "thead9", "hello",
//                reactions1, true);
//        HashMap<String, Message> mList1 = new HashMap<>();
//        mList1.put(m1.getMessageKey(), m1);
//        mList1.put(m2.getMessageKey(), m2);
//        Conversation conv1 = new Conversation("conv1", "2018-03-9 03:00:22.012", memberStatus1, mList1);
//
//        Map<String, Status> memberStatus2 = new HashMap<>();
//        memberStatus2.put("thead9", new Status(true, true));
//        memberStatus2.put("barney", new Status(false, false));
//        Reactions ur3 = new Reactions(new int[] {1, 2}, "thead9");
//        Reactions ur4 = new Reactions(new int[] {3, 4}, "barney");
//        Map<String, Reactions> reactions2 = new HashMap<>();
//        reactions2.put("thead9", ur3);
//        reactions2.put("barney", ur4);
//        Message m3 = new Message("2018-01-9 03:00:21.012", "2018-01-9 03:00:22.012",
//                new String[] {"thead9", "barney"}, "m3", "nvj4", "thead9", "hi",
//                reactions2, true);
//        Message m4 = new Message("2018-01-8 03:00:21.012", "2018-01-8 03:00:22.012",
//                new String[] {"thead9", "suzy"}, "m4", "nvj4", "thead9", "hi",
//                reactions2, true);
//        HashMap<String, Message> mList2 = new HashMap<>();
//        mList2.put(m3.getMessageKey(), m3);
//        mList2.put(m4.getMessageKey(), m4);
//        Conversation conv2 = new Conversation("conv2", "2017-03-9 03:00:22.012", memberStatus2, mList2);
//
//        HashMap<String, Conversation> cList = new HashMap<>();
//        cList.put(conv1.getConversationKey(), conv1);
//        cList.put(conv2.getConversationKey(), conv2);
//
//        NotificationLoggedInMessage m = new NotificationLoggedInMessage("thead9",
//                new Profile("Thomas Headley", "thead9@ufl.edu", "4074086638"), contactList, cList);
//
//        return m;
//    }

}