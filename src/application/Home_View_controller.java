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
import model.Profile;

import java.time.LocalDate;
import java.util.HashMap;

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

//        if(!contactList.isEmpty()){
//            for(Contact value: contactList.values()){
//                createNewContact(value);
//            }
//        }


        //TODO: import current conversations
    }

    public void createNewContact(Contact user){
        TitledPane newContact = new TitledPane();
        newContact.setText(user.getUsername());
        newContact.setStyle("-fx-background-color: #E7DECD");

        StackPane user_icon = new StackPane();
        user_icon.setPrefHeight(20);
        user_icon.setPrefWidth(20);
        user_icon.setMaxWidth(20);

//        Circle icon = new Circle(12);
//        Paint color =Paint.valueOf(user.getColor());
//        icon.setFill(color);
//
//        Label initial = new Label();
//        initial.setText("" + user.getUsername().charAt(0));
//        initial.setStyle("-fx-font: 10 system");
//
//        user_icon.getChildren().addAll(icon, initial);

        VBox content = new VBox();
        Label dm = new Label("Direct Message");
        dm.setCursor(Cursor.HAND);
        dm.setLabelFor(directMessage);
        dm.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try{
                    OpenDirectMessage(event, user.getUsername());
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
                    ViewOtherProfile(event, user.getUsername());
                } catch(Exception e){}
            }
        });

        content.setStyle("-fx-background-color: #E7DECD");

        content.getChildren().add(dm);
        content.getChildren().add(vp);


        newContact.setContent(content);
        newContact.setGraphic(user_icon);
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
            view = FXMLLoader.load(getClass().getResource("/application/viewCurrentUser.fxml"));
            split.getItems().set(1, view);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void Logout(ActionEvent event){
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
                loader.load();

                Search_View_Controller searchScreen = loader.getController();
                searchScreen.passConnection(connection);
                searchScreen.setSearchField(search.getText());
                connection.setDelegate(searchScreen);

                Parent root = loader.getRoot();
                Stage searchStage = new Stage();
                Scene scene = new Scene(root, 600, 400);
                scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
                searchStage.setScene(scene);
                searchStage.show();

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
            loader.load();

            Conversation_View_controller dmScreen = loader.getController();
            dmScreen.passConnection(connection);
            connection.setDelegate(dmScreen);
            dmScreen.setUsername(user);
            //TODO: pass the user to initialize the message screen

            Parent root = loader.getRoot();
            Stage dmStage = new Stage();
            Scene scene = new Scene(root, 552, 372);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            dmStage.setScene(scene);
            dmStage.show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void ViewOtherProfile(MouseEvent actionEvent, String user) throws Exception{
        try{
            FXMLLoader loader = new FXMLLoader();
            //TODO: change out the place holder fxml for view profile
            loader.setLocation(getClass().getResource("/application/home.fxml"));
            loader.load();

            Home_View_controller vpScreen = loader.getController();
            vpScreen.passConnection(connection);
            vpScreen.setUsername(user);
            connection.setDelegate(vpScreen);

            Parent root = loader.getRoot();
            Stage vpStage = new Stage();
            Scene scene = new Scene(root, 600, 400);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            vpStage.setScene(scene);
            vpStage.show();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void notification(ServerMessage message) {
        switch (message.getStatus()){
            case NOTIFICATIONPROFILEUPDATED:
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        //setValues();
                    }
                });
                break;
            default:
                break;
        }
    }
}
