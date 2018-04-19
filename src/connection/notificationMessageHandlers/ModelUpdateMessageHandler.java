package connection.notificationMessageHandlers;

import application.ViewController;
import connection.serverMessages.ServerMessage;
import model.UserUpdater;

public abstract class ModelUpdateMessageHandler {
    private UserUpdater userUpdater;

    public ModelUpdateMessageHandler(UserUpdater userUpdater) {
        this.userUpdater = userUpdater;
    }

    public void updateUser(ServerMessage serverMessage) {
        try {
            userUpdater.updateUser(serverMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
