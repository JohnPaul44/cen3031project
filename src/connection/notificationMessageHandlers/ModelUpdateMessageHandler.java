package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
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
        // userUpdater.updateUser(serverMessage);
    }
}
