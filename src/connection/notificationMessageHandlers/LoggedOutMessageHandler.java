package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
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
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }
        delegate.loggedOutNotification(errorInformation);
    }
}
