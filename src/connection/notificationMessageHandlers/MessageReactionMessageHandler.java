package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationMessageReaction;
import model.UserUpdater;

public class MessageReactionMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationMessageReaction serverMessage;

    public MessageReactionMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationMessageReaction) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        delegate.messageReactionNotification(errorInformation, serverMessage.getConversationKey(),
                serverMessage.getMessageKey(), serverMessage.getReactions());
        updateUser(serverMessage);
    }
}
