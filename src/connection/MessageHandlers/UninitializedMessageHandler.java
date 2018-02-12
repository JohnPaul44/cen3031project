package connection.MessageHandlers;

public class UninitializedMessageHandler implements NotificationMessageHandler {
    @Override
    public void handle() {
        System.out.println("Received an uninitialized message from the server");
    }
}
