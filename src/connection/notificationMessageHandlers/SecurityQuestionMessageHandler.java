package connection.notificationMessageHandlers;

import application.ViewController;
import connection.serverMessages.ServerMessage;

public class SecurityQuestionMessageHandler implements MessageHandler {
    private ServerMessage serverMessage;

    public SecurityQuestionMessageHandler(ServerMessage messageFromServer) {
        this.serverMessage = messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {

    }
}
