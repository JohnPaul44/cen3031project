package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class Search_View_Controller extends ViewController{

    ServerConnection connection;
    public void passConnection(ServerConnection con){
        connection = con;
    }

    @FXML
    private TextField searchField;
    @FXML
    private GridPane searchResults;


    public void setSearchField(String s){
        searchField.setText(s);
    }

    public void setSearchResults(int numResults,String user, String name, String email, String DOB ) {
        System.out.println(numResults + user + name + email + DOB);
        searchResults.addRow(numResults++,new Label(user), new Label(name), new Label(email), new Label(DOB));
    }

    //TODO: function that searches and returns the information from users

    @Override
    public void notification(ServerMessage message) {

    }
}
