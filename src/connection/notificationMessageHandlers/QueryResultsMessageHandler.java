package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationQueryResultsMessage;

public class QueryResultsMessageHandler implements MessageHandler {
    private NotificationQueryResultsMessage serverMessage;

    public QueryResultsMessageHandler(ServerMessage messageFromServer) {
        this.serverMessage = (NotificationQueryResultsMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        delegate.queryResultsNotification(errorInformation, serverMessage.getResults());
    }
}
