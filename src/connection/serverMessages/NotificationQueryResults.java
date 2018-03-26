package connection.serverMessages;

import model.Profile;

import java.util.HashMap;

public class NotificationQueryResults extends ServerMessage{
    private HashMap<String, Profile> results;


    public NotificationQueryResults(HashMap results) {
        this.status = Status.NOTIFICATIONQUERYRESULTS.ordinal();
        this.results = results;
    }

    public HashMap getResults() {return results;}
}
