package connection.serverMessages;

public class NotificationContactAddedMessage extends ServerMessage {
    private String username;
    private int level;
    private String firstName;
    private String lastName;
    private String email;
    private String birthday;
    private String gender;
    private String mind;
    private String bio;
    public NotificationContactAddedMessage(String username) {
        this.status = Status.NOTIFICATIONCONTACTADDED.ordinal();
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
