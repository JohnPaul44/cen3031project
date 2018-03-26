import connection.ServerConnection;
import connection.serverMessages.actionMessages.ActionLogInMessage;
import org.junit.Test;


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
