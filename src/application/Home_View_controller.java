package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Profile;

import java.time.LocalDate;

public class Home_View_controller extends ViewController{

    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        setValues();
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

    private void setUsername(String user){
        username.setText(user);
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
        if(birth == null){
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
            Stage registerStage = new Stage();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

            //closes the old screen when the new screen pops up
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
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
            Stage registerStage = new Stage();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

            //closes the login screen when the home screen pops up
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        } catch (Exception e) {
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
