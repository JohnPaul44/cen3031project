package connection;

import com.google.gson.Gson;
import connection.serverMessages.ServerMessage;

import java.util.ArrayList;

public class ServerTestDouble {
    private Gson gson;
    private ArrayList<ServerMessage> serverMessages;
    private ServerMessage.Status statusOfLastMessageReceived;

    public ServerTestDouble() {
        this.gson = new Gson();
        serverMessages = new ArrayList<>();
    }

    public ArrayList<ServerMessage> getServerMessages() { return serverMessages; }

    public ServerMessage.Status getStatusOfLastMessageReceived() {
        return statusOfLastMessageReceived;
    }

    public void receiveMessage(ServerMessage message) {
        System.out.println("ServerTestDouble received: " + gson.toJson(message));
        statusOfLastMessageReceived = message.getStatus();
    }

    public void addMessageToSend(ServerMessage serverMessage) {
        serverMessages.add(serverMessage);
    }
}

