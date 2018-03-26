package connection.notificationMessageHandlers;

import application.ViewController;

public class UninitializedMessageHandler implements MessageHandler {
    @Override
    public void handle(ViewController delegate) {
        System.out.println("Received an uninitialized message from the server");
    }
}
