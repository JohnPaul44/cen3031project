package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Search_View_Controller extends ViewController{

    ServerConnection connection;
    public void passConnection(ServerConnection con){
        connection = con;
    }

    @FXML
    private TextField searchField;

    public void setSearchField(String s){
        searchField.setText(s);
    }

    //TODO: function that searches and returns the information from users
    
    @Override
    public void notification(ServerMessage message) {

    }
}
