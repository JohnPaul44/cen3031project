package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Profile;

import java.util.HashMap;

public class NotificationQueryResultsMessage extends ServerMessage {
    private HashMap<String, Profile> results;


    public NotificationQueryResultsMessage(HashMap<String, Profile> results) {
        this.status = Status.NOTIFICATIONQUERYRESULTS.ordinal();
        this.results = results;
    }

    public HashMap<String, Profile> getResults() {return results;}
}
