package application;

import connection.ErrorInformation;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Profile;
import model.Reactions;

import javax.swing.*;
import java.util.Map;

public class Conversation_View_controller extends ViewController {
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
    @FXML
    private Label username;
    @FXML
    private AnchorPane anchor;

    public void setUsername(String user){
        username.setText(user);
    }

    public void sendMessageClicked (ActionEvent event) throws Exception {

        AnchorPane pane = new AnchorPane();
        TextArea new_message = new TextArea();
        new_message.setText(yourMessageField.getText());
        new_message.setWrapText(true);
        new_message.setEditable(false);
        new_message.setStyle("-fx-padding: 0 10 0 300");

        JEditorPane dummyEP = new JEditorPane();
        dummyEP.setSize(100, Short.MAX_VALUE);
        dummyEP.setText(yourMessageField.getText());
        new_message.setPrefHeight(dummyEP.getPreferredSize().height);
        new_message.setMinHeight(30);

        pane.setPrefHeight(dummyEP.getPreferredSize().height);

        pane.getChildren().add(new_message);
        box.getChildren().add(pane);

        yourMessageField.setText("");
        scroll.vvalueProperty().bind(box.heightProperty());
    }
    public void receivedMessage(){
        AnchorPane receivedPane = new AnchorPane();
        TextArea received_message = new TextArea();
        received_message.setText(yourMessageField.getText());
        received_message.setWrapText(true);
        received_message.setEditable(false);
        received_message.setStyle("-fx-padding: 0 300 0 10");

        JEditorPane dummyEP = new JEditorPane();
        dummyEP.setSize(100, Short.MAX_VALUE);
        dummyEP.setText(yourMessageField.getText());
        received_message.setPrefHeight(dummyEP.getPreferredSize().height);
        received_message.setMinHeight(30);

        receivedPane.setPrefHeight(dummyEP.getPreferredSize().height);

        receivedPane.getChildren().add(received_message);
        box.getChildren().add(receivedPane);
        scroll.vvalueProperty().bind(box.heightProperty());
    }

    @FXML
    private void initialize(){
    }

    @Override
    public void messageReceivedNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                            String time, String from, String text, Map<String, Reactions> reactions) {

    }
    @Override
    public void messageUpdatedNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                           String text) {

    }
    @Override
    public void messageReactionNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                            Map<String, Reactions> reactions) {

    }
    @Override
    public void messageReadNotification(ErrorInformation errorInformation, String conversationKey, String from) {

    }
    @Override
    public void typingNotification(ErrorInformation errorInformation, String conversationKey, String from, boolean typing) {

    }
}
