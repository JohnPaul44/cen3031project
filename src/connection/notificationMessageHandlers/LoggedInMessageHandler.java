package connection.notificationMessageHandlers;

import application.ViewController;
import connection.serverMessages.ServerMessage;
import model.UserUpdater;

public class LoggedInMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private ServerMessage messageFromServer;

    public LoggedInMessageHandler(ServerMessage serverMessage, UserUpdater userUpdater) {
        super(userUpdater);
        this.messageFromServer = serverMessage;
    }

    @Override
    public void handle(ViewController delegate) {
        updateUser(messageFromServer);
    }
}
