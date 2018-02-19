package connection.notificationMessageHandlers;

import com.google.gson.Gson;

interface NotificationMessageHandler {
    Gson gson = new Gson();
    void handle();
}
