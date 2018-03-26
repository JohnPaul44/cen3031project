package connection.notificationMessageHandlers;

import application.ViewController;
import connection.serverMessages.ServerMessage;
import model.UserUpdater;

public class LoggedOutMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private ServerMessage serverMessage;

    public LoggedOutMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        updateUser(serverMessage);
    }
}
