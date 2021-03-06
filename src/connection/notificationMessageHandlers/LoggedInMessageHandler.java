package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import model.UserUpdater;

public class LoggedInMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private ServerMessage serverMessage;

    public LoggedInMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = messageFromServer;
        System.out.println(messageFromServer.toString());
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }
        delegate.loggedInNotification(errorInformation);
    }
}
