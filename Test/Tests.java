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
