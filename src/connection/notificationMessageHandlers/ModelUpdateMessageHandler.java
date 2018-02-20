package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.MessageFactory;
import connection.serverMessages.ServerMessage;
import model.CurrentUser;
import model.UserUpdater;

public class ModelUpdateMessageHandler implements MessageHandler{
    private JsonObject jsonObject;
    private UserUpdater userUpdater;

    public ModelUpdateMessageHandler(JsonObject jsonObject, UserUpdater userUpdater) {
        this.jsonObject = jsonObject;
        this.userUpdater = userUpdater;
    }

    @Override
    public void handle() {
        // TODO Message factory

        MessageFactory messageFactory = new MessageFactory();
        try {
            ServerMessage serverMessage = messageFactory.produce(jsonObject);
            userUpdater.updateUser(serverMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // userUpdater.updateUser(serverMessage);
    }
}
