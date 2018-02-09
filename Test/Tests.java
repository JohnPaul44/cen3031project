import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import connection.Server;
import connection.ServerConnection;
import connection.serverMessaging.*;
import model.Profile;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import java.io.IOException;

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
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionRegisterMessage m = new ActionRegisterMessage("thead9", "bogus,", "Thomas Headley", "thead9@ufl.edu");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogInMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionLogInMessage m = new ActionLogInMessage("thead9", "bogus,");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendLogOutMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionLogOutMessage m = new ActionLogOutMessage();

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddContactMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionAddContactMessage m = new ActionAddContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveContactMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionRemoveContactMessage m = new ActionRemoveContactMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendUpdateProfileMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        Profile p = new Profile("Thomas Headley", "thead9", "4074086638");
        ActionUpdateProfileMessage m = new ActionUpdateProfileMessage(p);

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSendMessageMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionSendMessageMessage m = new ActionSendMessageMessage(ActionSendMessageMessage.ActionSendMessageMessageType.TO, "suzy", "Hi Suzy!");
        ActionSendMessageMessage m2 = new ActionSendMessageMessage(ActionSendMessageMessage.ActionSendMessageMessageType.CONVERSATIONKEY, "51dcj", "Hi Suzy!");

        conn.getOut().println(m.toJsonString());
        conn.getOut().println(m2.toJsonString());
    }

    @Test
    public void sendUpdateMessageMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionUpdateMessageMessage m = new ActionUpdateMessageMessage("14dv", "8dco", "Hello Suzy");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendAddUserToConversationMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionAddUserToConversationMessage m = new ActionAddUserToConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendRemoveUserFromConversationMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionRemoveUserFromConversationMessage m = new ActionRemoveUserFromConversationMessage("thead9", "dcn4");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendReadMessageMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionReadMessageMessage m = new ActionReadMessageMessage("thead9");

        conn.getOut().println(m.toJsonString());
    }

    @Test
    public void sendSetTypingMessage() {
        startTestServer();

        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        ActionSetTypingMessage m = new ActionSetTypingMessage("thead9", true);

        conn.getOut().println(m.toJsonString());
    }



    private void startTestServer(){
        Thread thread = new Thread(() -> {
            try {
                Server s = new Server();
                s.startServer();
            } catch (IOException e) {
                System.out.println("Error starting server in test: " + e);
            }
        });
        thread.start();
    }

}
