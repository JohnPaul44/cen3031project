package connection.notificationMessageHandlers;

import application.ViewController;
import connection.serverMessages.ServerMessage;

public class PasswordChangedMessageHandler implements MessageHandler {
    private ServerMessage serverMessage;

    public PasswordChangedMessageHandler(ServerMessage messageFromServer) {
        this.serverMessage = messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {

    }
}
