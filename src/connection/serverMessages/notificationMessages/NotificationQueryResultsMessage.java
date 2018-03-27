package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Profile;

import java.util.HashMap;

public class NotificationQueryResultsMessage extends ServerMessage {
    private HashMap<String, Profile> queryResults;


    public NotificationQueryResultsMessage(HashMap results) {
        this.status = Status.NOTIFICATIONQUERYRESULTS.ordinal();
        this.queryResults = results;
    }

    public HashMap<String, Profile> getResults() {return queryResults;}
}
