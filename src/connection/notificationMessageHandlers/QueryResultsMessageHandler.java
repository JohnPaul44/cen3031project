package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationQueryResultsMessage;

public class QueryResultsMessageHandler implements MessageHandler {
    private NotificationQueryResultsMessage queryResults;

    public QueryResultsMessageHandler(ServerMessage messageFromServer) {
        this.queryResults = (NotificationQueryResultsMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (queryResults.error()) {
            errorInformation.setErrorInformation(queryResults);
        }
        delegate.queryResultsNotification(errorInformation, queryResults.getResults());
    }
}
