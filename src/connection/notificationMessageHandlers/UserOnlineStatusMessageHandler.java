package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationUserOnlineStatusMessage;
import model.UserUpdater;

public class UserOnlineStatusMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationUserOnlineStatusMessage serverMessage;

    public UserOnlineStatusMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationUserOnlineStatusMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }
        delegate.userOnlineStatusNotification(errorInformation, serverMessage.getUsername(), serverMessage.getOnline());
    }
}
