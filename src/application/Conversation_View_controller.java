package application;

import connection.ServerConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.*;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

public class Conversation_View_controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        box.setSpacing(3);
        setTopic();
    }

    @FXML
    private TextArea yourMessageField;
    @FXML
    private Button sendButton;
    @FXML
    private VBox box;
    @FXML
    private ScrollPane scroll;
    @FXML
    private Label username;
    @FXML
    private TextField status;
    @FXML
    private TextField topic;

    private String thisUser;
    private boolean typing;


    private String[] convoTopics = {"Will technology save the human race or destroy it?", "What was the last movie you watched?", "What is the most overrated movie?",
            "What was your favorite book as a child?", "Who are the three greatest athletes of all time?", "Where would you like to travel next?", "What was the best invention of the last 50 years?",
            "What are your goals for the next 2 years?", "If you could call anyone in the world, who would you call?",
            "What is the oddest job you have had?", "What would you do if you won the lottery?", "What are five things you couldn't live without?",
            "What was the high-point and low-point of your day so far?", "What is your biggest fear?", "Do you think we should live like we're dying?",
            "Have you ever met one of your heroes?", "What do you value most: free time, recognition, or money?",
            "What’s the best compliment you’ve ever received?" , "Would you rather be a lonely genius, or a sociable idiot?", "What are you most grateful for, right now, in this moment?"};

    public void setTopic(){
        Random rand = new Random();
        int n = rand.nextInt(20);
        topic.setText(convoTopics[n]);
    }

    private String convKey = "";

    public void setUsername(String user){
        thisUser = user;
        username.setText(user);
        typing = false;
    }

    public void setStatus(String stat){
        status.setText(stat);
        status.setEditable(false);
    }

    public void setStatusRead(){
        status.setText(status.getText() + " - Read");
    }


    private Timer timer(){
        Timer time = new Timer();
        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                typing = false;
                connection.setTyping(convKey, false);
            }
        };
        time.schedule(task, 1500);
        return time;
    }

    private Timer t;
    private String prevMess;
    private String align = "";
    public void userTyping(){
        if(!typing){
            typing = true;
            connection.setTyping(convKey, true);
            t = timer();
        }
        else{
            t.cancel();
            t = timer();
        }
    }

    public void setTypingStatus(String from){
        prevMess = status.getText();
        System.out.println("alignment " + status.getAlignment());
        if(status.getAlignment() == Pos.CENTER_RIGHT){
            align = "r";
        }
        status.setAlignment(Pos.CENTER_LEFT);
        status.setEditable(false);
        status.setText(from + " is typing...");
    }

    public void notTyping(){
        if(align.equals("r")){
            status.setAlignment(Pos.CENTER_RIGHT);
            align = "";
        }
        status.setEditable(false);
        status.setText(prevMess);
    }


    public void setConversationKey(String convokey){
        convKey = convokey;
        if (!convKey.isEmpty()) {
            if(!connection.getCurrentUser().getConversationList().get(convokey).getMemberStatus().get(connection.getCurrentUser().getUserName()).getRead()){
                connection.readMessage(convKey);
            }
        }
    }

    public String getConvKey(){
        return convKey;
    }

    @FXML
    public void SendEventKey(KeyEvent keyEvent){
        if(keyEvent.getCode() == KeyCode.ENTER) {
            //sets typing to false
            connection.setTyping(convKey, false);
            //calls the same action that occurs when the button is pressed
            ActionEvent aevent = new ActionEvent(keyEvent.getSource(), sendButton);
            //pass the keyEvent into the button action event
            sendMessageClicked(aevent);
            keyEvent.consume();
        }
    }

    public void sendMessageClicked (ActionEvent event) {
        if (!yourMessageField.getText().isEmpty()) {
            String message = yourMessageField.getText();
            //Setting the text to blank here to improve responsiveness -Lincoln
            yourMessageField.setText("");
            //sentMessage(message);
            if (convKey.isEmpty()) {
                ArrayList<String> mess = new ArrayList<>();
                mess.add(username.getText());
                connection.sendFirstMessage(mess, message);
            } else {
                connection.sendMessage(convKey, message);
            }
        }
    }

    private void sentMessage(String text){
        AnchorPane pane = new AnchorPane();
        TextArea new_message = new TextArea();
        new_message.setText(text);
        new_message.setWrapText(true);
        new_message.setEditable(false);
        new_message.setMinWidth(644);
        new_message.setStyle("-fx-padding: 5 10 1 375; -fx-background-color: transparent; -fx-control-inner-background:#698F3F; fx-highlight-fill: #ffffff; -fx-highlight-text-fill: #ffffff; -fx-text-fill: #ffffff; ");

        JEditorPane dummyEP = new JEditorPane();
        dummyEP.setSize(100, Short.MAX_VALUE);
        dummyEP.setText(text);
        new_message.setPrefHeight(dummyEP.getPreferredSize().height);
        new_message.setMinHeight(35);

        pane.setPrefHeight(dummyEP.getPreferredSize().height);

        pane.getChildren().add(new_message);
        box.getChildren().add(pane);

        scroll.vvalueProperty().bind(box.heightProperty());
        }

    private void receivedMessage(String text){
        AnchorPane receivedPane = new AnchorPane();
        TextArea received_message = new TextArea();
        received_message.setText(text);
        received_message.setWrapText(true);
        received_message.setEditable(false);
        received_message.setMinWidth(644);
        received_message.setStyle("-fx-padding: 2 375 2 10; -fx-background-color: transparent; ");

        JEditorPane dummyEP = new JEditorPane();
        dummyEP.setSize(100, Short.MAX_VALUE);
        dummyEP.setText(text);
        received_message.setPrefHeight(dummyEP.getPreferredSize().height);
        received_message.setMinHeight(35);

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

        boolean readConvo = connection.getCurrentUser().getConversationList().get(convKey).getMemberStatus().get(thisUser).getRead();

        for(Message values : messagesList){
            String time = convertTimeView(values.getServerTime(), false);
            if(values.getFrom().equals(connection.getCurrentUser().getUserName())){
                sentMessage(values.getText());
                status.setAlignment(Pos.CENTER_RIGHT);
                status.setEditable(false);
                if(readConvo){
                    status.setText("Message Delivered " + time + " - Read");
                }
                else{
                    status.setText("Message Delivered " + time);
                }
            }
            else{
                receivedMessage(values.getText());
                status.setAlignment(Pos.CENTER_LEFT);
                status.setEditable(false);
                status.setText("Message Received " + time);
            }
        }
    }

    public void newMessage(String conversationKey, String messageKey,
                           String time, String from, String text, Map<String, Status> mem){

        time = convertTimeView(time, true);
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

    private String convertTimeView(String serverTime, boolean newM){

        String newTime = serverTime.substring(5,7) + "/" + serverTime.substring(8,10) + "/" + serverTime.substring(2,4);

        int hour = Integer.parseInt(serverTime.substring(11,13));
        String ampm = "";
        if(!newM){
            hour = hour - 4;
        }

        if(hour > 12){
            hour = hour - 12;
            ampm = "pm";
        }
        else{
            ampm = "am";
        }

       newTime = newTime + " " + hour + ":" + serverTime.substring(14,16) + " " + ampm;
        return newTime;
    }
}
