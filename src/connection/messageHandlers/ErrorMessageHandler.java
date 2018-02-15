package connection.messageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessaging.NotificationErrorMessage;

public class ErrorMessageHandler implements NotificationMessageHandler{
    private NotificationErrorMessage serverMessage;

    public ErrorMessageHandler(JsonObject messageFromServer) {
        this.serverMessage = gson.fromJson(messageFromServer, NotificationErrorMessage.class);
    }

    @Override
    public void handle() {
        System.out.println("Error serverMessage received from server");
        System.out.println("Error Number: " + serverMessage.getErrorNumber() + " Error String: ");
    }
}
