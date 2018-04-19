package connection.serverMessages.actionMessages;

public class ActionGetFriendshipStatsMessage extends ActionServerMessage {
    private String username;

    public ActionGetFriendshipStatsMessage(String username) {
        this.status = Status.ACTIONGETFRIENDSHIPSTATISTICS.ordinal();
        this.username = username;
    }
}
