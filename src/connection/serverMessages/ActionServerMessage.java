package connection.serverMessages;

import model.Globals;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class ActionServerMessage extends ServerMessage {
    String clientTime;

    public ActionServerMessage() {
        Date date = new Date();
        this.clientTime = new SimpleDateFormat(Globals.simpldDateFormat).format(date);
    }
}
