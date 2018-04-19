package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationProfileUpdatedMessage;
import model.UserUpdater;

public class ProfileUpdatedMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationProfileUpdatedMessage serverMessage;

    public ProfileUpdatedMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationProfileUpdatedMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }
        delegate.profileUpdatedNotification(errorInformation, serverMessage.getProfile());
    }
}
