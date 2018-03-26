package connection.notificationMessageHandlers;

import application.ViewController;
import com.google.gson.Gson;

public interface MessageHandler {
    Gson gson = new Gson();
    void handle(ViewController delegate);
}
