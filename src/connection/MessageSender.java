package connection;

import com.google.gson.Gson;
import connection.serverMessages.ActionLogInMessage;
import connection.serverMessages.ActionRegisterMessage;
import connection.serverMessages.ServerMessage;

import java.io.PrintWriter;

public class MessageSender {
    private PrintWriter out;
    private Gson gson;

    public MessageSender(PrintWriter out) {
        this.out = out;
        this.gson = new Gson();
    }

    public void sendMessage(ServerMessage message) {
        switch (message.getStatus()) {
            case 14:
                ActionRegisterMessage actionRegisterMessage = (ActionRegisterMessage)message;
                out.println(actionRegisterMessage.toJsonString());
                break;
            case 15:
                ActionLogInMessage actionLogInMessage = (ActionLogInMessage)message;
                out.println(actionLogInMessage.toJsonString());
                break;
            default:
                System.out.println("Invalid message was attempted to be sent with status: " + message.getStatus());
        }
    }
}
