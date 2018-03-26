package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationUserRemovedFromConversationMessage;
import model.UserUpdater;

public class UserRemovedFromConversationMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationUserRemovedFromConversationMessage serverMessage;

    public UserRemovedFromConversationMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationUserRemovedFromConversationMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        delegate.userRemovedFromConversationNotification(errorInformation, serverMessage.getUsername(), serverMessage.getConversationKey(),
                serverMessage.getConversation());
        updateUser(serverMessage);
    }
}
