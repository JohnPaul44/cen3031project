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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    private String thisUser;


    String[] convoTopics = {"Will technology save the human race or destroy it?", "What was the last movie you watched?", "What is the most overrated movie?",
            "What was your favorite book as a child?", "Who are the three greatest athletes of all time?", "Where would you like to travel next?", "What was the best invention of the last 50 years?",
            "What are your goals for the next 2 years?", "If you could call anyone in the world, who would you call?",
            "What is the oddest job you have had?", "What would you do if you won the lottery?", "What are five things you couldn't live without?",
            "What was the high-point and low-point of your day so far?", "What is your biggest fear?", "Do you think we should live like we're dying?",
            "Have you ever meet one of your heroes?", "What do you value most: free time, recognition, or money?",
            "What’s the best compliment you’ve ever received?" , "Would you rather be a lonely genius, or a sociable idiot?", "What are you most grateful for, right now, in this moment?"};

    public void setTopic(){
        Random rand = new Random();
        int n = rand.nextInt(20);
        topic.setText(convoTopics[n]);
    }

    public String convKey = "";

    public void setUsername(String user){
        thisUser = user;
        username.setText(user);
    }

    public void setStatus(String stat){
        status.setText(stat);
        status.setEditable(false);
    }

    public void setConversationKey(String convokey){
        convKey = convokey;
        if (!convKey.isEmpty()) connection.readMessage(convKey);
    }

    @FXML
    public void SendEventKey(KeyEvent keyEvent) throws Exception{
        if(keyEvent.getCode() == KeyCode.ENTER) {
            //calls the same action that occurs when the button is pressed
            ActionEvent aevent = new ActionEvent(keyEvent.getSource(), sendButton);
            //pass the keyEvent into the button action event
            sendMessageClicked(aevent);
            keyEvent.consume();
        }
    }

    public void sendMessageClicked (ActionEvent event) throws Exception {
        if (!yourMessageField.getText().isEmpty()) {
            String message = yourMessageField.getText();
            //Setting the text to blank here to improve responsiveness -Lincoln
            yourMessageField.setText("");
            //sentMessage(message);
            if (convKey.isEmpty()) {
                ArrayList<String> mess = new ArrayList<String>();
                mess.add(username.getText());
                connection.sendFirstMessage(mess, message);
            } else {
                connection.sendMessage(convKey, message);
            }
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

        scroll.vvalueProperty().bind(box.heightProperty());

        //this is called elsewhere now and probably is redundant. It is being left in just in case -Lincoln
        yourMessageField.setText("");
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
        if(convKey.isEmpty()) {
            return;
        }

        Conversation convo = connection.getCurrentUser().getConversationList().get(convKey);
        Collection<Message> messagesColl = convo.getMessages().values();
        List<Message> messagesList = new ArrayList(messagesColl);
        Collections.sort(messagesList);

        for(Message values : messagesList){
            if(values.getFrom().equals(connection.getCurrentUser().getUserName())){
                sentMessage(values.getText());
                status.setAlignment(Pos.CENTER_RIGHT);
                status.setEditable(false);
                status.setText("Message Delivered " + values.getServerTime());
            }
            else{
                receivedMessage(values.getText());
                status.setAlignment(Pos.CENTER_LEFT);
                status.setEditable(false);
                status.setText("Message Received " + values.getServerTime());
            }
        }
    }

    public void newMessage(String conversationKey, String messageKey,
                           String time, String from, String text, Map<String, Status> mem){

        if(mem.keySet().contains(thisUser)){
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
