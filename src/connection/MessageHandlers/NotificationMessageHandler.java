package connection.MessageHandlers;

import com.google.gson.Gson;

interface NotificationMessageHandler {
    Gson gson = new Gson();
    void handle();
}
