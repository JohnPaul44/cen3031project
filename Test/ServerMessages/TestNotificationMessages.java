package ServerMessages;

import connection.ServerConnectionTestDouble;
import connection.ServerTestDouble;
import connection.serverMessages.*;
import model.Contact;
import model.Conversation;
import model.Message;
import model.Status;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class TestNotificationMessages {

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
    public void receiveErrorMessage() {
        NotificationErrorMessage message = new NotificationErrorMessage(dummyData.errorNumber, dummyData.errorMessage);
        server.addMessageToSend(message);

        connection.listenToServer();
        assertTrue(connection.getStatusOfLastMessageReceived().equals(ServerMessage.Status.NOTIFICATIONERROR));
    }

    @Test
    public void receiveLoggedInMessage() {
        NotificationLoggedInMessage message = createLoggedInMessage();
        server.addMessageToSend(message);

        connection.listenToServer();
        assertTrue(connection.getStatusOfLastMessageReceived().equals(ServerMessage.Status.NOTIFICATIONLOGGEDIN));
        // TODO make oop
        assertTrue(connection.getCurrentUser().getUserName().equals(dummyData.username1));
    }

    @Test
    public void receiveSecurityQuestionMessage() {
        NotificationSecurityQuestion securityQuestion = new NotificationSecurityQuestion(dummyData.securityQuestion1);
        server.addMessageToSend(securityQuestion);

        connection.listenToServer();
        assertTrue(connection.getStatusOfLastMessageReceived().equals(ServerMessage.Status.NOTIFICATIONSECURITYQUESTION));
    }

    @Test
    public void receivePasswordChangedMessage() {
        NotificationPasswordChanged message = new NotificationPasswordChanged();
        server.addMessageToSend(message);

        connection.listenToServer();
        assertTrue(connection.getStatusOfLastMessageReceived().equals(ServerMessage.Status.NOTIFICATIONPASSWORDCHANGED));
    }

    // TODO tests for rest of NotificationMessages including changepassword

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
