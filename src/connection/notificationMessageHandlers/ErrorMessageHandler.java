package connection.notificationMessageHandlers;

import application.ViewController;
import connection.serverMessages.NotificationErrorMessage;
import connection.serverMessages.ServerMessage;

public class ErrorMessageHandler implements MessageHandler {
    private NotificationErrorMessage serverMessage;

    public ErrorMessageHandler(ServerMessage messageFromServer) {
        this.serverMessage = (NotificationErrorMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {

        System.out.println("Error serverMessage received from server");
        System.out.println("Error Number: " + serverMessage.getErrorNumber());
        System.out.println("Error String: " + serverMessage.getErrorString());
    }
}
