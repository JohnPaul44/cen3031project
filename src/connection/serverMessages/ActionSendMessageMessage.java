package connection.serverMessages;

import model.Globals;
import model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ActionSendMessageMessage extends ActionServerMessage {
    private Message message;

    public ActionSendMessageMessage(Message message) {
        this.status = Status.ACTIONSENDMESSAGE.ordinal();

        this.message = message;
        setMessageClientTime();
    }

    private void setMessageClientTime() {
        message.setClientTime(clientTime);
    }
}
