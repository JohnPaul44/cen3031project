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
import javafx.geometry.Pos;
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
import model.*;

import javax.swing.*;
import javax.xml.soap.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Conversation_View_controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        setTopic();
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
    @FXML
    private TextField status;
    @FXML
    private TextField topic;

    String[] convoTopics = {"Will technology save the human race or destroy it?", "What was the last movie you watched?", "What is the most overrated movie?",
    "What was your favorite book as a child?", "Who are the three greatest athletes of all time?", "Where would you like to travel next?",
    "What was the best invention of the last 50 years?", "What are your goals for the next 2 years?", "If you could call anyone in the world, who would you call?",};

    public void setTopic(){
        Random rand = new Random();
        int n = rand.nextInt(9);
        topic.setText(convoTopics[n]);
    }

    public String convKey = "";

    public void setUsername(String user){
        username.setText(user);
    }

    public void setConversationKey(String convokey){
        convKey = convokey;
    }

    @FXML
    public void SendEventKey(KeyEvent keyEvent) throws Exception{
        if(keyEvent.getCode() == KeyCode.ENTER) {
            //calls the same action that occurs when the button is pressed
            ActionEvent aevent = new ActionEvent(keyEvent.getSource(), sendButton);
            //pass the keyEvent into the button action event
            sendMessageClicked(aevent);
        }
    }

    public void sendMessageClicked (ActionEvent event) throws Exception {
        String message = yourMessageField.getText();
        //sentMessage(message);
        if(convKey.isEmpty()){
            ArrayList<String> mess = new ArrayList<String>();
            mess.add(username.getText());
            connection.sendFirstMessage(mess, message);
        }
        else{
            connection.sendMessage(convKey, message);
        }
    }

    public void sentMessage(String text){
        AnchorPane pane = new AnchorPane();
        TextArea new_message = new TextArea();
        new_message.setText(text);
        new_message.setWrapText(true);
        new_message.setEditable(false);
        new_message.setMinWidth(644);
        new_message.setStyle("-fx-padding: 5 10 0 375");

        JEditorPane dummyEP = new JEditorPane();
        dummyEP.setSize(100, Short.MAX_VALUE);
        dummyEP.setText(text);
        new_message.setPrefHeight(dummyEP.getPreferredSize().height);
        new_message.setMinHeight(30);

        pane.setPrefHeight(dummyEP.getPreferredSize().height);

        pane.getChildren().add(new_message);
        box.getChildren().add(pane);

        yourMessageField.setText("");
        scroll.vvalueProperty().bind(box.heightProperty());
    }

    public void receivedMessage(String text){
        AnchorPane receivedPane = new AnchorPane();
        TextArea received_message = new TextArea();
        received_message.setText(text);
        received_message.setWrapText(true);
        received_message.setEditable(false);
        received_message.setMinWidth(644);
        received_message.setStyle("-fx-padding: 5 375 0 10");

        JEditorPane dummyEP = new JEditorPane();
        dummyEP.setSize(100, Short.MAX_VALUE);
        dummyEP.setText(text);
        received_message.setPrefHeight(dummyEP.getPreferredSize().height);
        received_message.setMinHeight(30);

        receivedPane.setPrefHeight(dummyEP.getPreferredSize().height);

        receivedPane.getChildren().add(received_message);
        box.getChildren().add(receivedPane);
        scroll.vvalueProperty().bind(box.heightProperty());
    }

    @FXML
    public void setMessages(){
        if(convKey.isEmpty()){
            return;
        }

        Conversation convo = connection.getCurrentUser().getConversationList().get(convKey);
        HashMap<String, Message> messages = convo.getMessages();
        for(Message values : messages.values()){
            if(values.getFrom().equals(connection.getCurrentUser().getUserName())){
                sentMessage(values.getText());
            }
            else{
                receivedMessage(values.getText());
            }
        }
    }

    @Override
    public void messageReceivedNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                            String time, String from, String text) {
        if(errorInformation.getErrorNumber() == 0){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Map<String, Status> mem = connection.getCurrentUser().getConversationList().get(conversationKey).getMemberStatus();
                    if(mem.keySet().contains(username.getText())){
                        if(convKey.isEmpty()){
                            setConversationKey(conversationKey);
                        }
                        if(from.equals(username.getText())){
                            receivedMessage(text);
                            status.setAlignment(Pos.CENTER_LEFT);
                            status.setEditable(false);
                            status.setText("Message Received " + time);
                        }
                        else{
                            sentMessage(text);
                            status.setAlignment(Pos.CENTER_RIGHT);
                            status.setEditable(false);
                            status.setText("Message Delivered " + time);
                        }
                    }
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
        }
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
        if(errorInformation.getErrorNumber() == 0){

        }
    }
    @Override
    public void typingNotification(ErrorInformation errorInformation, String conversationKey, String from, boolean typing) {
        if(errorInformation.getErrorNumber() == 0 && typing){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    status.setAlignment(Pos.CENTER_LEFT);
                    status.setEditable(false);
                    status.setText("..." + from + " is typing...");
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
        }
    }
}
