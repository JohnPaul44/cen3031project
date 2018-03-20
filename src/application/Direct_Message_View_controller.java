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
import javafx.scene.layout.VBox;
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
    @FXML
    private VBox box;
    @FXML
    private ScrollPane scroll;

    ObservableList<String> messages = FXCollections.observableArrayList();

    public void sendMessageClicked (ActionEvent event) throws Exception {
        TextArea new_message = new TextArea();
        new_message.setText(yourMessageField.getText());
        new_message.setWrapText(true);
        new_message.setEditable(false);
        new_message.setStyle("-fx-padding: 0 10 0 300");
        box.getChildren().add(new_message);
        yourMessageField.setText("");
        scroll.vvalueProperty().bind(box.heightProperty());
    }

    @FXML
    private void initialize(){
//        chatWindow.setItems(messages);
    }

    @Override
    public void notification(ServerMessage message) {
    }
}
