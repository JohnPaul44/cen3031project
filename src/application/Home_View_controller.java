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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Contact;
import model.Conversation;
import model.Message;
import model.Profile;

import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.HashMap;

public class Home_View_controller extends ViewController{

    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        setValues();
        setConversationsList();
    }

    public void setValues(){
        setUsername(connection.getCurrentUser().getUserName());
        setFirstName(connection.getCurrentUser().getProfile().getName());
        setLastName(connection.getCurrentUser().getProfile().getLastName());
        setEmail(connection.getCurrentUser().getProfile().getEmail());
        setPhone(connection.getCurrentUser().getProfile().getPhone());
        setGender(connection.getCurrentUser().getProfile().getGender());
        setBirthday(connection.getCurrentUser().getProfile().getBirthday());
    }

    public void setConversationsList(){
        HashMap<String, Contact> contactList = connection.getCurrentUser().getContactList();

//        if(!contactList.isEmpty()){
//            for(Contact value: contactList.values()){
//                createNewContact(value.getUsername());
//            }
//        }

        createNewContact("testing");
        createNewContact("another");
        createNewContact("one more");
        //TODO: import current conversations
    }

    //Create a new conversation using conversation/message API
//    public String createNewConversation(String[] users, String text) {
//     return Conversation.createConversation(Message.createFirstMessage(users, text));
//    }

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

//        String[] users = {connection.getCurrentUser().getUserName(), user};
//        String greeting = connection.getCurrentUser().getProfile().getName() + " " + connection.getCurrentUser().getProfile().getLastName() + " (" + connection.getCurrentUser().getUserName() + ") has added you as a friend!";
//        String conversationKey = createNewConversation(users, greeting);
    }

    @FXML
    private Label username;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField email;
    @FXML
    private TextField phone;
    @FXML
    private ChoiceBox gender;
    @FXML
    private DatePicker birthday;
    @FXML
    private TextArea bio;
    @FXML
    private TextField mind;
    @FXML
    private TextArea interests;
    @FXML
    private TextArea hobbies;
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

    private void setUsername(String user){
        username.setText(user);
        usernameAcc.setText(user);
    }

    private void setFirstName(String first){
        firstName.setText(first);
    }

    private void setLastName(String last){
        lastName.setText(last);
    }

    private void setEmail(String e){
        email.setText(e);
    }

    private void setPhone(String phoneNum){
        phone.setText(phoneNum);
    }

    private void setGender(String gen){
        if(gen.equalsIgnoreCase("female")){
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.FEMALE);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.FEMALE);
        }
        else if(gen.equalsIgnoreCase("male")){
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.MALE);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.MALE);
        }
        else if(gen.equalsIgnoreCase("other")){
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.OTHER);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.OTHER);
        }
        else{
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.NA);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.NA);
        }
    }

    private void setBirthday(String birth){
        if(birth.equals("null")){
            return;
        }
        LocalDate birthdate = LocalDate.parse(birth);
        birthday.setValue(birthdate);
    }


    @FXML
    public void EditProfile(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/createProfile.fxml"));
            loader.load();

            EditProfile_View_Controller edit = loader.getController();
            edit.passConnection(connection);
            connection.setDelegate(edit);

            Parent root = loader.getRoot();
            Stage registerStage = (Stage) firstName.getScene().getWindow();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

            //closes the old screen when the new screen pops up
            //((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
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
            Stage registerStage = (Stage) firstName.getScene().getWindow();
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

            Direct_Message_View_controller dmScreen = loader.getController();
            dmScreen.passConnection(connection);
            connection.setDelegate(dmScreen);
            //TODO: pass the user to initialize the message screen

            Parent root = loader.getRoot();
            Stage dmStage = new Stage();
            Scene scene = new Scene(root, 600, 400);
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
            loader.setLocation(getClass().getResource("/application/viewProfile.fxml"));
            loader.load();

            ViewProfile_View_Controller vpScreen = loader.getController();
            vpScreen.passConnection(connection);
            vpScreen.setUsername(user);
            connection.setDelegate(vpScreen);

            Parent root = loader.getRoot();
            Stage vpStage = new Stage();
            Scene scene = new Scene(root, 700, 600);
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
