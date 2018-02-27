package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;

public class EditProfile_View_Controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
    }
    @Override
    public void notification(ServerMessage message) {

    }
}
