package ServerMessages;

import connection.ServerConnectionTestDouble;
import connection.ServerTestDouble;
import connection.serverMessages.*;
import connection.serverMessages.actionMessages.*;
import connection.serverMessages.notificationMessages.NotificationContactAddedMessage;
import connection.serverMessages.notificationMessages.NotificationFriendshipStatsMessage;
import connection.serverMessages.notificationMessages.NotificationLoggedInMessage;
import junit.framework.TestCase;
import model.Contact;
import model.Conversation;
import model.Message;
import model.Status;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestsMessages {
    ServerTestDouble server;
    ServerConnectionTestDouble connection;
    DummyData dummyData;

    @Before
    public void setUp() {
        server = new ServerTestDouble();
        connection = new ServerConnectionTestDouble(server);
        dummyData = new DummyData();
    }

    @Test
    public void sendFriendshipStatsMessage() {
        NotificationLoggedInMessage loggedInMessage = createLoggedInMessage();
        NotificationFriendshipStatsMessage friendshipStatsMessage = new NotificationFriendshipStatsMessage(dummyData.username2,dummyData.friendshipStats1);
        ArrayList<ServerMessage> serverMessages = new ArrayList<>();
        serverMessages.add(loggedInMessage);
        serverMessages.add(friendshipStatsMessage);

        sendAndReceiveMessages(serverMessages);
        Assert.assertTrue(connection.getCurrentUser().getContactList().get(dummyData.username2).getFriendshipStats().getFriendshipLevel()==1);
        assertTrue(connection.getCurrentUser().getContactList().get(dummyData.username2).getFriendshipStats().getSentMessages()==3);
        assertTrue(connection.getCurrentUser().getContactList().get(dummyData.username2).getFriendshipStats().getReceivedMessages()==2);
    }

    private NotificationLoggedInMessage createLoggedInMessage() {
        HashMap<String, Contact> contactList = new HashMap<>();
        contactList.put(dummyData.username1, dummyData.contact1);
        contactList.put(dummyData.username2, dummyData.contact2);
        contactList.put(dummyData.username3, dummyData.contact3);

        Map<String, Status> memberStatus1 = new HashMap<>();
        memberStatus1.put(dummyData.username1, new Status(true, true));
        memberStatus1.put(dummyData.username2, new Status(false, false));

        HashMap<String, Message> messageList1 = new HashMap<>();
        messageList1.put(dummyData.messageKey1, dummyData.message1);
        messageList1.put(dummyData.messageKey2, dummyData.message2);

        Conversation conversation1 = new Conversation(dummyData.conversationKey1, "2018-03-9 03:00:22.012",
                memberStatus1, messageList1);


        Map<String, Status> memberStatus2 = new HashMap<>();
        memberStatus1.put(dummyData.username1, new Status(true, true));
        memberStatus1.put(dummyData.username3, new Status(false, false));

        HashMap<String, Message> messageList2 = new HashMap<>();
        messageList1.put(dummyData.messageKey3, dummyData.message3);
        messageList1.put(dummyData.messageKey4, dummyData.message4);

        Conversation conversation2 = new Conversation(dummyData.conversationKey2, "2018-03-9 03:00:22.012",
                memberStatus2, messageList2);

        HashMap<String, Conversation> conversationList = new HashMap<>();
        conversationList.put(dummyData.conversationKey1, conversation1);
        conversationList.put(dummyData.conversationKey2, conversation2);

        NotificationLoggedInMessage message = new NotificationLoggedInMessage(dummyData.username1, dummyData.profile,
                contactList, conversationList);

        return message;
    }

    private void sendAndReceiveMessages(ArrayList<ServerMessage> messages) {
        for (ServerMessage message : messages) {
            server.addMessageToSend(message);
            connection.listenToServer();
        }
    }
}
