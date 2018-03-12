package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

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
    private Label firstName;
    @FXML
    private Label lastName;
    @FXML
    private Label email;
    @FXML
    private Label phone;
    @FXML
    private Label gender;
    @FXML
    private Label birthday;
    @FXML
    private Label bio;
    @FXML
    private Label mind;
    @FXML
    private Label interests;
    @FXML
    private Label hobbies;

    public void setUsername(String user){
        username.setText(user);
    }

    public void setFirstName(String first){
        firstName.setText(first);
    }

    public void setLastName(String last){
        lastName.setText(last);
    }

    public void setEmail(String e){
        email.setText(e);
    }

    public void setPhone(String phoneNum){
        phone.setText(phoneNum);
    }

    public void setGender(String gen){
        gender.setText(gen);
    }

    public void setBirthday(String birth){
        if(birth == null){
            birthday.setText("N/A");
        }
        else{
            birthday.setText(birth);
        }
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
