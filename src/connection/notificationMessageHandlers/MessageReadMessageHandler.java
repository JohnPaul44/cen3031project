package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationMessageReadMessage;
import model.UserUpdater;

public class MessageReadMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationMessageReadMessage serverMessage;

    public MessageReadMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationMessageReadMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        if(serverMessage.getConversationKey()!=null) {
            updateUser(serverMessage);
            delegate.messageReadNotification(errorInformation, serverMessage.getConversationKey(), serverMessage.getFrom());
        }
    }
}
