package connection;

import application.ViewController;
import connection.notificationMessageHandlers.HandlerFactory;
import connection.notificationMessageHandlers.MessageHandler;
import connection.serverMessages.ServerMessage;
import model.CurrentUser;
import model.UserUpdater;

public class ServerConnectionTestDouble implements IServerConnection{
    private ServerTestDouble serverTestDouble;
    private CurrentUser currentUser;
    private ServerMessage.Status statusOfLastMessageReceived;
    private ViewController delegate = new ViewController() {
        @Override
        public void notification(ServerMessage message) {

        }
    };

    public ServerConnectionTestDouble(ServerConnectionTestDouble serverConnectionTestDouble) {
        this.serverTestDouble = new ServerTestDouble();
    }

    public ServerConnectionTestDouble(ServerTestDouble serverTestDouble) {
        this.serverTestDouble = serverTestDouble;
        this.currentUser = new CurrentUser();
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public ServerMessage.Status getStatusOfLastMessageReceived() {
        return statusOfLastMessageReceived;
    }

    @Override
    public void sendMessageToServer(ServerMessage serverMessage) {
        serverTestDouble.receiveMessage(serverMessage);
    }

    @Override
    public void listenToServer() {
        HandlerFactory handlerFactory = new HandlerFactory();
        UserUpdater userUpdater = new UserUpdater(currentUser);
        for (ServerMessage serverMessage : serverTestDouble.getServerMessages()) {
            try {
                statusOfLastMessageReceived = serverMessage.getStatus();
                MessageHandler handler = handlerFactory.produce(serverMessage, userUpdater);
                handler.handle(delegate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        serverTestDouble.getServerMessages().clear();
    }
}
