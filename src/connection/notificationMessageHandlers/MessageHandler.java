package connection.notificationMessageHandlers;

import com.google.gson.Gson;

public interface MessageHandler {
    Gson gson = new Gson();
    void handle();
}
