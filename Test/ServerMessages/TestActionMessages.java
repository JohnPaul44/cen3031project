package ServerMessages;

import connection.ServerConnectionTestDouble;
import connection.ServerTestDouble;
import connection.serverMessages.*;
import model.Profile;
import model.Reactions;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class TestActionMessages {

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
    public void sendRegisterMessage() {
        ActionRegisterMessage message = new ActionRegisterMessage(dummyData.username, dummyData.password, dummyData.firstName,
                dummyData.lastName, dummyData.email, dummyData.phone, dummyData.gender, dummyData.birthday,
                dummyData.securityQuestion, dummyData.securityAnswer);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONREGISTER));
    }

    @Test
    public void sendLogInMessage() {
        ActionLogInMessage message = new ActionLogInMessage(dummyData.username, dummyData.password);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONLOGIN));
    }

    @Test
    public void sendRequestSecurityQuestionMessage() {
        ActionRequestSecurityQuestion message = new ActionRequestSecurityQuestion(dummyData.username);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONREQUESTSECURITYQUESTION));
    }

    @Test
    public void sendChangePasswordMessage() {
        ActionChangePassword message = new ActionChangePassword(dummyData.username, dummyData.securityAnswer, dummyData.phone);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONCHANGEPASSWORD));
    }

    @Test
    public void sendLogOutMessage() {
        ActionLogOutMessage message = new ActionLogOutMessage();

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONLOGOUT));
    }

    @Test
    public void sendAddContactMessage() {
        ActionAddContactMessage message = new ActionAddContactMessage(dummyData.username);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONADDCONTACT));
    }

    @Test
    public void sendRemoveContactMessage() {
        ActionRemoveContactMessage message = new ActionRemoveContactMessage(dummyData.username);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONREMOVECONTACT));
    }

    @Test
    public void sendUpdateProfileMessage() {
        ActionUpdateProfileMessage message = new ActionUpdateProfileMessage(dummyData.profile);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONUPDATEPROFILE));
    }

    @Test
    public void sendSendMessageMessageUsingToUsername() {
        ActionSendMessageMessage message = new ActionSendMessageMessage(
                ActionSendMessageMessage.ActionSendMessageMessageType.TO,
                dummyData.username, dummyData.messageText1);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONSENDMESSAGE));
    }

    @Test
    public void sendSendMessageMessageUsingToConversationKey() {
        ActionSendMessageMessage message = new ActionSendMessageMessage(
                ActionSendMessageMessage.ActionSendMessageMessageType.CONVERSATIONKEY,
                dummyData.conversationKey1, dummyData.messageText1);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONSENDMESSAGE));
    }

    @Test
    public void sendUpdateMessageMessage() {
        ActionUpdateMessageMessage message = new ActionUpdateMessageMessage(dummyData.conversationKey1, dummyData.messageKey1,
                dummyData.messageText1);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONUPDATEMESSAGE));
    }

    @Test
    public void sendReactToMessage() {
        ActionReactToMessage message = new ActionReactToMessage(dummyData.conversationKey1, dummyData.messageKey1,
                dummyData.reactions1);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONREACTTOMESSAGE));
    }

    @Test
    public void sendAddUserToConversationMessage() {
        ActionAddUserToConversationMessage message = new ActionAddUserToConversationMessage(dummyData.username,
                dummyData.conversationKey1);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONADDUSERTOCONVERSATION));
    }

    @Test
    public void sendRemoveUserFromConversationMessage() {
        ActionRemoveUserFromConversationMessage message = new ActionRemoveUserFromConversationMessage(dummyData.username,
                dummyData.conversationKey1);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONREMOVEDUSERFROMCONVERSATION));
    }

    @Test
    public void sendReadMessageMessage() {
        ActionReadMessageMessage message = new ActionReadMessageMessage(dummyData.conversationKey1);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONREADMESSAGE));
    }

    @Test
    public void sendSetTypingMessage() {
        ActionSetTypingMessage message = new ActionSetTypingMessage(dummyData.conversationKey1, true);

        connection.sendMessageToServer(message);
        assertTrue(server.getStatusOfLastMessageReceived().equals(ServerMessage.Status.ACTIONSETTYPING));
    }

    class DummyData {
        private String username = "thead9";
        private String password = "bogus";
        private String firstName = "Thomas";
        private String lastName = "Headley";
        private String email = "thead9@ufl.edu";
        private String phone = "4074086638";
        private Profile.Gender gender = Profile.Gender.MALE;
        private String birthday = "06/14/1995";
        private String securityQuestion = "Who are you?";
        private String securityAnswer = "Me";

        private Profile profile;

        private String conversationKey1 = "conversationKey1";
        private String messageText1 = "Message Text";
        private String messageKey1 = "messageKey1";

        private Map<String, Reactions> reactions1 = new HashMap<>();
        private Reactions specificReactions1 = new Reactions(new int[]{1, 2}, username);

        DummyData() {
            profile = new Profile(firstName, lastName, email, phone, securityQuestion, securityAnswer, gender, birthday);
            setUpReactions();
        }

        private void setUpReactions() {
            reactions1.put(username, specificReactions1);
        }
    }
}
