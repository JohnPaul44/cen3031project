package connection.notificationMessageHandlers;


import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationUserAddedToConversationMessage;
import model.UserUpdater;

public class UserAddedToConversationMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationUserAddedToConversationMessage serverMessage;

    public UserAddedToConversationMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationUserAddedToConversationMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        delegate.userAddedToConversationNotification(errorInformation, serverMessage.getUsername(), serverMessage.getConversationKey());
        updateUser(serverMessage);
    }
}
