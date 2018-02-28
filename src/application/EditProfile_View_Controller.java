package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class EditProfile_View_Controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
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
    private TextField gender;
    @FXML
    private TextField dob;
    @FXML
    private TextArea interest;
    @FXML
    private TextArea hobbies;
    @FXML
    private Button save;

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
        if(birth.isEmpty()){
            dob.setText("N/A");
        }
        else{
            dob.setText(birth);
        }
    }

    public void setMind(String mindStatement){
        mind.setText(mindStatement);
    }

    public void setBio(String bioStatement){
        bio.setText(bioStatement);
    }

    public void setInterests(String interestList){
        interest.setText(interestList);
    }

    public void setHobbies(String hob){
        hobbies.setText(hob);
    }

    //initializes all the information on the profile
//    @FXML
//    public void initialize(){
//        setUsername(connection.getCurrentUser().getUserName());
//        setFirstName(connection.getCurrentUser().getProfile().getName());
//        setLastName(connection.getCurrentUser().getProfile().getLastName());
//        setEmail(connection.getCurrentUser().getProfile().getEmail());
//        setPhone(connection.getCurrentUser().getProfile().getPhone());
//        setGender(connection.getCurrentUser().getProfile().getGender());
//        setBirthday(connection.getCurrentUser().getProfile().getBirthday());
//
//        //TODO: set the bio, what's on your mind, interests, hobbies. update the profile settings
//    }

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
        //has limited error checking
        //will update at a later time

        if(!firstName().isEmpty()){
            connection.getCurrentUser().getProfile().setName(firstName());
        }
        if(!lastName().isEmpty()){
            connection.getCurrentUser().getProfile().setLastName(lastName());
        }
        if(!email().isEmpty()){
            connection.getCurrentUser().getProfile().setEmail(email());
        }
        if(!phone().isEmpty()){
            connection.getCurrentUser().getProfile().setPhone(phone());
        }
        if(!gender().isEmpty()){
            connection.getCurrentUser().getProfile().setGender(gender());
        }
        if(!birthday().isEmpty()){
            connection.getCurrentUser().getProfile().setBirthday(birthday());
        }

        connection.updateProfile();

        //TODO: setting the bio, whats on your mind, interest and hobbies

        //call the function to open the home screen up
        BackButton(event);
    }

    public String firstName(){
        return firstName.getText();
    }

    public String lastName(){
        return lastName.getText();
    }

    public String email(){
        return email.getText();
    }

    public String phone(){
        return phone.getText();
    }

    public String gender(){
        return gender.getText();
    }

    public String birthday(){
        return dob.getText();
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

    }
}
