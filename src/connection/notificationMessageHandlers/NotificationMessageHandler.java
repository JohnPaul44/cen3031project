package connection.notificationMessageHandlers;

import com.google.gson.Gson;

public interface NotificationMessageHandler {
    Gson gson = new Gson();
    void handle();
}
