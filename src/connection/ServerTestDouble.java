package connection;

import com.google.gson.Gson;
import connection.serverMessages.ServerMessage;

public class ServerTestDouble {
    private Gson gson;
    private ServerMessage[] serverMessages;
    private ServerMessage.Status statusOfLastMessageReceived;

    public ServerTestDouble() {
        this.gson = new Gson();
        serverMessages = new ServerMessage[]{};
    }

    public ServerTestDouble(ServerMessage[] serverMessages){
        this.gson = new Gson();
        this.serverMessages = serverMessages;
    }

    public ServerMessage[] getServerMessages() { return serverMessages; }

    public ServerMessage.Status getStatusOfLastMessageReceived() {
        return statusOfLastMessageReceived;
    }

    public void receiveMessage(ServerMessage message) {
        System.out.println("ServerTestDouble received: " + gson.toJson(message));
        statusOfLastMessageReceived = message.getStatus();
    }

    public ServerMessage sendMessage(int index) {
        return serverMessages[index];
    }

}

