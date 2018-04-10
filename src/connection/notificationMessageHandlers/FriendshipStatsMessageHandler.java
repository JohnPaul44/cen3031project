package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationFriendshipStatsMessage;
import model.UserUpdater;

public class FriendshipStatsMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationFriendshipStatsMessage serverMessage;

    public FriendshipStatsMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationFriendshipStatsMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        updateUser(serverMessage);
        delegate.friendshipStatsNotification(errorInformation, serverMessage.getUsername(), serverMessage.getFriendshipStats());
    }
}
