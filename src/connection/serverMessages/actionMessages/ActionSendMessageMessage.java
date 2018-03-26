package connection.serverMessages.actionMessages;

import connection.serverMessages.ServerMessage;
import model.Message;

public class ActionSendMessageMessage extends ActionServerMessage {
    private Message message;

    public ActionSendMessageMessage(Message message) {
        this.status = ServerMessage.Status.ACTIONSENDMESSAGE.ordinal();

        this.message = message;
        setMessageClientTime();
    }

    private void setMessageClientTime() {
        message.setClientTime(clientTime);
    }
}
