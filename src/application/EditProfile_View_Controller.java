package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.Profile;

import java.time.LocalDate;

public class EditProfile_View_Controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        setUsername(connection.getCurrentUser().getUserName());
        setFirstName(connection.getCurrentUser().getProfile().getName());
        setLastName(connection.getCurrentUser().getProfile().getLastName());
        setEmail(connection.getCurrentUser().getProfile().getEmail());
        setPhone(connection.getCurrentUser().getProfile().getPhone());
        genderField.setItems(genderFieldList);
        setGender(connection.getCurrentUser().getProfile().getGender());
        setBirthday(connection.getCurrentUser().getProfile().getBirthday());
    }

    @FXML
    private Label username;
    @FXML
    private TextField mind;
    @FXML
    private TextArea bio;
    @FXML
    private TextField firstName;
    @FXML
    private TextField lastName;
    @FXML
    private TextField email;
    @FXML
    private TextField phone;
    @FXML
    private ChoiceBox genderField;
    @FXML
    private DatePicker dob;
    @FXML
    private TextArea interest;
    @FXML
    private TextArea hobbies;
    @FXML
    private Button save;

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
            genderField.setValue(Profile.Gender.FEMALE);
        }
        else if(gen.equalsIgnoreCase("male")){
            genderField.setValue(Profile.Gender.MALE);
        }
        else if(gen.equalsIgnoreCase("other")){
            genderField.setValue(Profile.Gender.OTHER);
        }
        else{
            genderField.setValue(Profile.Gender.NA);
        }
    }

    private void setBirthday(String birth){
        if(birth == null){
            return;
        }
        LocalDate birthdate = LocalDate.parse(birth);
        dob.setValue(birthdate);
    }

    private void setMind(String mindStatement){
        mind.setText(mindStatement);
    }

    private void setBio(String bioStatement){
        bio.setText(bioStatement);
    }

    private void setInterests(String interestList){
        interest.setText(interestList);
    }

    private void setHobbies(String hob){
        hobbies.setText(hob);
    }

    //event handlers for both when the button is pressed or when the enter key is used
    @FXML
    public void SaveChangesEventKey(KeyEvent keyEvent) throws Exception{
        if(keyEvent.getCode() == KeyCode.ENTER) {
            //calls the same action that occurs when the button is pressed
            ActionEvent aevent = new ActionEvent(keyEvent.getSource(), save);
            //pass the keyEvent into the button action event
            SaveChangesButton(aevent);
        }
    }

    @FXML
    public void SaveChangesButton(ActionEvent event){

        //error checks if there is information in the required fields
        //if there are empty required fields or if the input information is not correct, it just doesn't update the profile
        //currently, does not alert the user of the incorrect information
        if(!firstName().isEmpty()){
            connection.getCurrentUser().getProfile().setName(firstName());
        }
        if(!lastName().isEmpty()){
            connection.getCurrentUser().getProfile().setLastName(lastName());
        }
        if(!email().isEmpty()){
            connection.getCurrentUser().getProfile().setEmail(email());
        }
        if(checkPhoneNumber()){
            connection.getCurrentUser().getProfile().setPhone(phone());
        }
        connection.getCurrentUser().getProfile().setBirthday(birthday());
        connection.getCurrentUser().getProfile().setGender(gender());

        connection.updateProfile();

        //TODO: setting the bio, whats on your mind, interest and hobbies

        //call the function to open the home screen up
        BackButton(event);
    }

    private String firstName(){
        return firstName.getText();
    }

    private String lastName(){
        return lastName.getText();
    }

    private String email(){
        return email.getText();
    }

    private String phone(){
        return phone.getText();
    }

    ObservableList<Profile.Gender> genderFieldList = FXCollections.observableArrayList(Profile.Gender.values());
    private String gender(){
        return genderField.getValue().toString();
    }

    private String birthday(){
        if(dob.getValue() == null){
            return "";
        }
        return dob.getValue().toString();
    }

    private boolean checkPhoneNumber() {
        String phoneNum = phone.getText();
        if (phoneNum.matches("[0-9]*") && !phoneNum.isEmpty() && phoneNum.length() == 10) {
            System.out.println("Phone # accepted!");
            return true;
        }
        else {
            System.out.println("Numbers only! Please re-enter a valid phone number!");
        }
        return false;
    }

    @FXML
    public void BackButton(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/home.fxml"));
            loader.load();

            Home_View_controller home = loader.getController();
            home.passConnection(connection);
            connection.setDelegate(home);

            Parent root = loader.getRoot();
            Stage registerStage = (Stage) save.getScene().getWindow();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notification(ServerMessage message) {

    }
}
