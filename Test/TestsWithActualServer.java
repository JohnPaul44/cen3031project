import connection.ServerConnection;
import connection.serverMessages.ActionLogInMessage;
import org.junit.Test;

import java.util.concurrent.TimeUnit;


public class TestsWithActualServer {

    @Test
    public void firstConnection() throws InterruptedException {
        ServerConnection conn = new ServerConnection("35.231.80.25", 8675);
        ActionLogInMessage message = new ActionLogInMessage("test_user", "test_password");
        conn.sendMessageToServer(message);
        while (true) {
        }
    }
}
