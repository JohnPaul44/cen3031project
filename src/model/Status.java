package model;

import connection.serverMessages.NotificationMessageReadMessage;
import connection.serverMessages.NotificationTypingMessage;
import connection.serverMessages.NotificationUserAddedToConversationMessage;

public class Status {
    /*Read   bool `json:"read"`
    Typing bool `json:"typing"`*/

    private boolean read;
    private boolean typing;

    // Test Constructor
    public Status(boolean read, boolean typing) {
        this.read = read;
        this.typing = typing;
    }

    public Status() {
        read = false;
        typing = false;
    }

    public boolean getRead() {
        return read;
    }
    public boolean getTyping() {
        return typing;
    }
    public void setRead(boolean read) { this.read = read; }
    public void setTyping(boolean typing) { this.typing = typing; }

    public void updateRead(NotificationMessageReadMessage message) {
        read = true;
    }

    public void updateTyping(NotificationTypingMessage message) {
        typing = message.getTyping();
    }
}
