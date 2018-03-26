package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationMessageReceivedMessage;
import model.UserUpdater;

public class MessageReceivedMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationMessageReceivedMessage serverMessage;

    public MessageReceivedMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationMessageReceivedMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        delegate.messageReceivedNotification(errorInformation, serverMessage.getConversationKey(), serverMessage.getMessageKey(),
                serverMessage.getServerTime(), serverMessage.getFrom(), serverMessage.getText(), serverMessage.getReactions());
        updateUser(serverMessage);
    }
}
