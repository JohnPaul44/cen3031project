package application;

import connection.ErrorInformation;
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
import java.util.ArrayList;
import java.util.HashMap;

public class ViewCurrentUser_View_controller extends ViewController {

    ServerConnection connection;

    public void passConnection(ServerConnection con) {
        connection = con;
        setValues();
    }

    public void setValues() {
        setUsername(connection.getCurrentUser().getUserName());
        setFirstName(connection.getCurrentUser().getProfile().getFirstName());
        setLastName(connection.getCurrentUser().getProfile().getLastName());
        setEmail(connection.getCurrentUser().getProfile().getEmail());
        setPhone(connection.getCurrentUser().getProfile().getPhone());
        setGender(connection.getCurrentUser().getProfile().getGender());
        setBirthday(connection.getCurrentUser().getProfile().getBirthday());
        setBio(connection.getCurrentUser().getProfile().getBio());
        setMind(connection.getCurrentUser().getProfile().getStatus());
        setInterests(connection.getCurrentUser().getProfile().getInterests());
        setHobbies(connection.getCurrentUser().getProfile().getHobbies());
        setIcon();
    }

    public void setIcon() {
        String first_letter = "" + connection.getCurrentUser().getUserName().charAt(0);
        icon_letter.setText(first_letter);

        Paint icon_color = Paint.valueOf(connection.getCurrentUser().getProfile().getColor());
        icon.setFill(icon_color);
        icon_design.setFill(icon_color);
        icon_design.setOpacity(0.4);
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
    private Circle icon;
    @FXML
    private Circle icon_design;
    @FXML
    private Label icon_letter;
    @FXML
    private Button editProfile;
    @FXML
    private AnchorPane anchor;

    private void setUsername(String user) {
        username.setText(user);
    }

    private void setFirstName(String first) {
        firstName.setText(first);
    }

    private void setLastName(String last) {
        lastName.setText(last);
    }

    private void setEmail(String e) {
        email.setText(e);
    }

    private void setPhone(String phoneNum) {
        phone.setText(phoneNum);
    }

    private void setGender(String gen) {
        if (gen.equalsIgnoreCase("female")) {
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.FEMALE);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.FEMALE);
        } else if (gen.equalsIgnoreCase("male")) {
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.MALE);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.MALE);
        } else if (gen.equalsIgnoreCase("other")) {
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.OTHER);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.OTHER);
        } else {
            ObservableList<Profile.Gender> genderList = FXCollections.observableArrayList(Profile.Gender.NA);
            gender.setItems(genderList);
            gender.setValue(Profile.Gender.NA);
        }
    }

    private void setBirthday(String birth) {
        if (birth == null || birth.equals("null") || birth.equals("")) {
            return;
        }
        LocalDate birthdate = LocalDate.parse(birth);
        birthday.setValue(birthdate);
    }

    private void setBio(String bioText) {
        bio.setText(bioText);
    }

    private void setMind(String status) {
        mind.setText(status);
    }

    private void setInterests(ArrayList<String> interestList) {
        interests.setText(ArrayListToString(interestList));
    }

    private void setHobbies(ArrayList<String> hob) {
        hobbies.setText(ArrayListToString(hob));
    }

    @FXML
    public void EditProfile() {

        FXMLLoader loadEdit = new FXMLLoader();
        loadEdit.setLocation(getClass().getResource("/application/createProfile.fxml"));
        AnchorPane temp = new AnchorPane();
        try {
            temp = loadEdit.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        anchor.getChildren().add(temp);

        EditProfile_View_Controller edit = loadEdit.getController();
        edit.passConnection(connection);
    }

    @FXML
    public void Logout(ActionEvent event) {
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
}
