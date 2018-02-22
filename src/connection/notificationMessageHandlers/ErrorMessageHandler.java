package connection.notificationMessageHandlers;

import com.google.gson.JsonObject;
import connection.serverMessages.NotificationErrorMessage;

public class ErrorMessageHandler implements MessageHandler {
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
