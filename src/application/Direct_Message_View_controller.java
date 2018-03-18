package application;

import connection.ServerConnection;
import connection.serverMessages.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.Profile;

public class Direct_Message_View_controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
    }

    @FXML
    private TextArea yourMessageField;
    @FXML
    private Button sendButton;
    @FXML
    private ListView chatWindow;
    @FXML
    private TextField userYou, userThem;

    ObservableList<String> messages = FXCollections.observableArrayList();

    public void sendMessageClicked (ActionEvent event) throws Exception {
        messages.add("User 1: " + userYou.getText());//get 1st user's text from his/her textfield and add message to observablelist
        userYou.setText("");//clear 1st user's textfield
    }

    @FXML
    private void initialize(){
        chatWindow.setItems(messages);
    }

    @Override
    public void notification(ServerMessage message) {
    }
}
