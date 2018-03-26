package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.Profile;

import java.util.HashMap;

public class NotificationQueryResults extends ServerMessage {
    private HashMap<String, Profile> results;


    public NotificationQueryResults(HashMap results) {
        this.status = Status.NOTIFICATIONQUERYRESULTS.ordinal();
        this.results = results;
    }

    public HashMap<String, Profile> getResults() {return results;}
}
