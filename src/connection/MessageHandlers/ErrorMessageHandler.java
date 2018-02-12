package connection.MessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationErrorMessage;

public class ErrorMessageHandler implements NotificationMessageHandler{
    private NotificationErrorMessage message;

    public ErrorMessageHandler(JsonObject messageFromServer) {
        this.message = gson.fromJson(messageFromServer, NotificationErrorMessage.class);
    }

    @Override
    public void handle() {
        System.out.println("Error message received from server");
        System.out.println("Error Number: " + message.getErrorNumber() + " Error String: ");
    }
}
