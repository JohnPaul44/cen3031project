package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.Contact;
import model.Conversation;
import model.Profile;
import sun.plugin.javascript.navig.Anchor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Home_View_controller extends ViewController{

    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        setConversationsList();
        setUsername(connection.getCurrentUser().getUserName());
        loadCurrentProfile();
    }

    public void setConversationsList(){
        HashMap<String, Contact> contactList = connection.getCurrentUser().getContactList();

        if(!contactList.isEmpty()){
            for(String key: contactList.keySet()){
                createNewContact(key);
            }
        }

        //TODO: import current conversations
    }

    public void createNewContact(String user){
        TitledPane newContact = new TitledPane();
        newContact.setText(user);
        newContact.setStyle("-fx-background-color: #E7DECD");

        VBox content = new VBox();
        Label dm = new Label("Direct Message");
        dm.setCursor(Cursor.HAND);
        dm.setLabelFor(directMessage);
        dm.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try{
                    OpenDirectMessage(event, user);
                } catch(Exception e){}
            }
        });

        Label vp = new Label("View Profile");
        vp.setCursor(Cursor.HAND);
        vp.setLabelFor(viewProfile);
        vp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try{
                    ViewOtherProfile(event, user);
                } catch(Exception e){}
            }
        });

        content.setStyle("-fx-background-color: #E7DECD");

        content.getChildren().add(dm);
        content.getChildren().add(vp);


        newContact.setContent(content);
        conversations.getPanes().add(newContact);
    }

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Accordion conversations;
    @FXML
    private TitledPane usernameAcc;
    @FXML
    private TextField search;
    @FXML
    private Label directMessage;
    @FXML
    private Label viewProfile;
    @FXML
    private SplitPane split;
    @FXML
    private AnchorPane view;


    private void setUsername(String user){
        usernameAcc.setText(user);
    }

    public void loadCurrentProfile(){
        try {
            AnchorPane temp = new AnchorPane();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/viewCurrentUser.fxml"));
            temp = loader.load();
            view.getChildren().add(temp);

            ViewCurrentUser_View_controller profile = loader.getController();
            profile.passConnection(connection);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void loadEditProfile(){
        try {
            AnchorPane temp = new AnchorPane();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/createProfile.fxml"));
            temp = loader.load();
            setView(temp);

            EditProfile_View_Controller edit = loader.getController();
            edit.passConnection(connection);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setView(AnchorPane anchor){
        System.out.println("setting the view");
        try{
            view.getChildren().add(anchor);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void Logout(){
        connection.logout();

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/login.fxml"));
            loader.load();

            Login_View_controller login = loader.getController();
            login.passConnection(connection);
            connection.setDelegate(login);

            Parent root = loader.getRoot();
            Stage registerStage = (Stage) scrollPane.getScene().getWindow();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void Search(javafx.scene.input.KeyEvent keyEvent) throws Exception{
        if(keyEvent.getCode() == KeyCode.ENTER) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/application/search.fxml"));
                AnchorPane anchor = new AnchorPane();
                anchor = loader.load();

                setView(anchor);

                Search_View_Controller searchScreen = loader.getController();
                searchScreen.passConnection(connection);
                searchScreen.setSearchField(search.getText());
                connection.setDelegate(searchScreen);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void OpenDirectMessage(MouseEvent actionEvent, String user) throws Exception{
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/directMessage.fxml"));
            AnchorPane anchor = new AnchorPane();
            anchor = loader.load();
            setView(anchor);

            Conversation_View_controller dmScreen = loader.getController();
            dmScreen.passConnection(connection);
            connection.setDelegate(dmScreen);
            dmScreen.setUsername(user);

            HashMap<String, Conversation> convos = connection.getCurrentUser().getConversationList();
            if(convos != null) {
                for (Map.Entry<String, Conversation> entry : convos.entrySet()) {
                    String key = entry.getKey();
                    Conversation value = entry.getValue();
                    if (value.getMemberStatus().containsKey(user)) {
                        dmScreen.setConversationKey(key);
                    }
                }
            }
            dmScreen.setMessages();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void ViewOtherProfile(MouseEvent actionEvent, String user) throws Exception{
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/viewProfile.fxml"));
            AnchorPane anchor = new AnchorPane();
            anchor = loader.load();
            setView(anchor);

            ViewProfile_View_Controller vpScreen = loader.getController();
            vpScreen.passConnection(connection);
            vpScreen.setUsername(user);
            vpScreen.setValuesContact();
            connection.setDelegate(vpScreen);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

//    @Override
//    public void notification(ServerMessage message) {
//        switch (message.getStatus()){
//            case NOTIFICATIONPROFILEUPDATED:
//                Platform.runLater(new Runnable(){
//                    @Override
//                    public void run(){
//                        //setValues();
//                    }
//                });
//                break;
//            default:
//                break;
//        }
//    }
}
