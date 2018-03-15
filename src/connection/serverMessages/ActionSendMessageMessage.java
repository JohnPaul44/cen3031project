package connection.serverMessages;

import model.Globals;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActionSendMessageMessage extends ActionServerMessage {
    public enum ActionSendMessageMessageType {TO, CONVERSATIONKEY}
    private String to;
    private String conversationKey;
    private String text;

    public ActionSendMessageMessage(ActionSendMessageMessageType type, String toOrConversationKey, String text) {
        this.status = Status.ACTIONSENDMESSAGE.ordinal();

        if (type == ActionSendMessageMessageType.TO) {
            this.to = toOrConversationKey;
        }
        if (type == ActionSendMessageMessageType.CONVERSATIONKEY) {
            this.conversationKey = toOrConversationKey;
        }

        this.text = text;
    }
}
