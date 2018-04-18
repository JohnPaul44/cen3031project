package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationContactRemovedMessage;
import model.UserUpdater;

public class ContactRemovedMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationContactRemovedMessage serverMessage;

    public ContactRemovedMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationContactRemovedMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }
        delegate.contactRemovedNotification(errorInformation, serverMessage.getUsername());
    }
}
