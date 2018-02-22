package connection;

import connection.serverMessages.ServerMessage;

public interface IServerConnection {
    void sendMessageToServer(ServerMessage serverMessage);
    void listenToServer();
}
