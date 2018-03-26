package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;

public class PasswordChangedMessageHandler implements MessageHandler {
    private ServerMessage serverMessage;

    public PasswordChangedMessageHandler(ServerMessage messageFromServer) {
        this.serverMessage = messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        delegate.passwordChangedNotification(errorInformation);
    }
}
