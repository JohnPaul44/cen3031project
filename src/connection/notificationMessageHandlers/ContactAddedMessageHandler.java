package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationContactAddedMessage;
import model.UserUpdater;

public class ContactAddedMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationContactAddedMessage serverMessage;

    public ContactAddedMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationContactAddedMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }
        delegate.contactAddedNotification(errorInformation, serverMessage.getUsername());
    }
}
