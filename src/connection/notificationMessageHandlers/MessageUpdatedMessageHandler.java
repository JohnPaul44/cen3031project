package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationMessageUpdatedMessage;
import model.UserUpdater;

public class MessageUpdatedMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationMessageUpdatedMessage serverMessage;

    public MessageUpdatedMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationMessageUpdatedMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        updateUser(serverMessage);
        delegate.messageUpdatedNotification(errorInformation, serverMessage.getConversationKey(), serverMessage.getMessageKey(),
                serverMessage.getText());
    }
}
