package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationContactUpdatedMessage;
import model.UserUpdater;

public class ContactUpdatedMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationContactUpdatedMessage serverMessage;

    public ContactUpdatedMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationContactUpdatedMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        updateUser(serverMessage);
        delegate.contactUpdatedNotification(errorInformation, serverMessage.getContacts());
    }
}
