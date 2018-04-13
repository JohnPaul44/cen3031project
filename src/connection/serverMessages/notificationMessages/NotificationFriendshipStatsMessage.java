package connection.serverMessages.notificationMessages;

import connection.serverMessages.ServerMessage;
import model.FriendshipStats;

public class NotificationFriendshipStatsMessage extends ServerMessage {

    private String username;
    private FriendshipStats friendshipStats;

    public NotificationFriendshipStatsMessage(String username, FriendshipStats friendshipStats) {
        this.status = Status.NOTIFICATIONFRIENDSHIPSTATS.ordinal();
        this.username = username;
        this.friendshipStats = friendshipStats;
    }

    public String getUsername() {
        return username;
    }

    public FriendshipStats getFriendshipStats() {
        if(friendshipStats == null) {
            friendshipStats = new FriendshipStats(0,0,0);
        }
        return friendshipStats;
    }
}
