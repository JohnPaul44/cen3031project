package model;

import connection.notificationMessageHandlers.*;
import connection.serverMessages.ServerMessage;

public class UserUpdater {
    private CurrentUser currentUser;

    public UserUpdater(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }
}
